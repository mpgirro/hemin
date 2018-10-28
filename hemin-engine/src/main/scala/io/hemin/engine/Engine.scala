package io.hemin.engine

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import io.hemin.engine.catalog.CatalogStore._
import io.hemin.engine.exception.HeminException
import io.hemin.engine.model._
import io.hemin.engine.node.Node
import io.hemin.engine.node.Node.{CliInput, CliOutput, EngineOperational, StartupStatus}
import io.hemin.engine.searcher.Searcher.{SearchRequest, SearchResults}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object Engine {
  final val name: String = "hemin"

  /** Try to boot an [[io.hemin.engine.Engine]] instance for the given
    * configuration map. The internal [[io.hemin.engine.EngineConfig]]
    * will be instantiated with the values from this map. All unspecified
    * properties of the `EngineConfig` members will have the internal
    * defaults. Akka configuration properties in the configuration
    * map will also be passed to the internal Akka system. */
  def boot(config: Config): Try[Engine] = Try(new Engine(config))

  /** Try to boot an [[io.hemin.engine.Engine]] instance for the
    * given configuration map. The internal actor system will have
    * the default Akka configuration. */
  def boot(config: EngineConfig): Try[Engine] = Try(new Engine(config))
}

class Engine private (engineConfig: EngineConfig, akkaConfig: Config) {

  def this(config: Config) = this(engineConfig = EngineConfig.load(config), akkaConfig = config)

  def this(config: EngineConfig) = this(engineConfig = config, akkaConfig = ConfigFactory.empty)

  /** Configuration of the Engine instance */
  val config: EngineConfig = engineConfig

  private val log = Logger(getClass)

  private lazy val running: AtomicBoolean = new AtomicBoolean(false)

  private implicit lazy val internalTimeout: Timeout = config.node.internalTimeout
  private implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(4)) //  TODO set parameters from config

  // lazy init the actor system and local bus for this node
  private[engine] val system: ActorSystem = ActorSystem(Engine.name, akkaConfig)
  private[engine] val node: ActorRef = system.actorOf(Props(new Node(config)), Node.name)

  private lazy val circuitBreaker: CircuitBreaker =
    (for {
      scheduler    <- Option(system).map(_.scheduler)
      maxFailures  <- Option(config).map(_.node.breakerMaxFailures)
      callTimeout  <- Option(config).map(_.node.breakerCallTimeout.duration)
      resetTimeout <- Option(config).map(_.node.breakerResetTimeout.duration)
    } yield CircuitBreaker(scheduler, maxFailures, callTimeout, resetTimeout)
      .onOpen(breakerOpen())
      .onClose(breakerClose())
      .onHalfOpen(breakerHalfOpen())).get


  // Run the startup sequence. This will throw an exception in case a Failure occurred
  startupSequence() match {
    case Success(_)  => log.info("ENGINE startup complete ...")
    case Failure(ex) => throw ex // escalate the construction fault, the factory method will wrap it in a Try-Failure
  }


  /** Attempts to shutdown the Engine. This operation is thread-safe.
    * It will produce a `Failure` if the Engine is not running. */
  def shutdown(): Try[Unit] = synchronized {
    if (running.get) {
      shutdownOnWarm()
    } else {
      shutdownOnCold()
    }
  }

  def propose(url: String): Try[Unit] = guarded {
    bus ! ProposeNewFeed(url)
  }

  /** Processes the arguments by the Command Language Interpreter,
    * and returns the resulting data as text */
  def cli(args: String): Future[String] = guarded {
    (bus ? CliInput(args))
      .mapTo[CliOutput]
      .map(_.output)
  }

  def search(query: String, page: Option[Int], size: Option[Int]): Future[ResultPage] = guarded {
    (bus ? SearchRequest(query, page, size))
      .mapTo[SearchResults]
      .map(_.results)
  }

  /** Finds a [[io.hemin.engine.model.Podcast]] by ID */
  def findPodcast(id: String): Future[Option[Podcast]] = guarded {
    (bus ? GetPodcast(id))
      .mapTo[PodcastResult]
      .map(_.podcast)
  }

  def findEpisode(id: String): Future[Option[Episode]] = guarded {
    (bus ? GetEpisode(id))
      .mapTo[EpisodeResult]
      .map(_.episode)
  }

  def findFeed(id: String): Future[Option[Feed]] = guarded {
    (bus ? GetFeed(id))
      .mapTo[FeedResult]
      .map(_.feed)
  }

  def findImage(id: String): Future[Option[Image]] = guarded {
    (bus ? GetImage(id))
      .mapTo[ImageResult]
      .map(_.image)
  }

  def findImageByAssociate(id: String): Future[Option[Image]] = guarded {
    (bus ? GetImageByAssociate(id))
      .mapTo[ImageResult]
      .map(_.image)
  }

  def findAllPodcasts(page: Option[Int], size: Option[Int]): Future[List[Podcast]] = guarded {
    (bus ? GetAllPodcastsRegistrationComplete(page, size))
      .mapTo[AllPodcastsResult]
      .map(_.podcasts)
  }

  def findEpisodesByPodcast(id: String): Future[List[Episode]] = guarded {
    (bus ? GetEpisodesByPodcast(id))
      .mapTo[EpisodesByPodcastResult]
      .map(_.episodes)
  }

  def findFeedsByPodcast(id: String): Future[List[Feed]] = guarded {
    (bus ? GetFeedsByPodcast(id))
      .mapTo[FeedsByPodcastResult]
      .map(_.feeds)
  }

  def findChaptersByEpisode(id: String): Future[List[Chapter]] = guarded {
    (bus ? GetChaptersByEpisode(id))
      .mapTo[ChaptersByEpisodeResult]
      .map(_.chapters)
  }

  /** The call to warmup() will tap the lazy values, and wait until all
    * subsystems in the actor hierarchy report that they are up and running */
  private def startupSequence(): Try[Unit] = /*synchronized*/ {
    log.info("ENGINE is starting up ...")
    warmup()
  }

  private def warmup(): Try[Unit] = /*blocking*/ {
    val startup = bus ? EngineOperational
    Await.result(startup, internalTimeout.duration) match {
      case StartupStatus(true) =>
        running.set(true)
        Success(Unit)
      case StartupStatus(false) =>
        Thread.sleep(25) // don't wait too busy
        warmup()
    }
  }

  private def shutdownOnWarm(): Try[Unit] = {
    log.info("ENGINE is shutting down ...")
    //bus ! ShutdownSystem // TODO does system.terminate() work better?
    running.set(false) // we always set running to false, because on error the engine is still screwed up
    val terminate = Await.result(system.terminate(), internalTimeout.duration)
    Try(terminate) match {
      case Success(_)  => Success(Unit)
      case Failure(ex) => Failure(shutdownError(ex))
    }
  }

  private def shutdownOnCold(): Try[Unit] = Failure(shutdownErrorEngineNotRunning)

  private[engine] def bus: ActorRef = node // TODO at some point I want to change this to something that distributes a message to the cluster

  private def guarded[T](body: => Future[T]): Future[T] =
    if (running.get) {
      circuitBreaker.withCircuitBreaker(body)
    } else {
      Future.failed(guardErrorEngineNotRunning)
    }

  private def guarded[T](body: => T): Try[T] =
    if (running.get) {
      Try(circuitBreaker.withSyncCircuitBreaker(body))
    } else {
      Failure(guardErrorEngineNotRunning)
    }

  // TODO unused?
  private def startupError(ex: Throwable): HeminException =
    new HeminException(s"Engine startup failed; reason : ${ex.getMessage}", ex)

  private lazy val guardErrorEngineNotRunning: HeminException =
    new HeminException("Guard prevented call; reason: Engine not running")

  private lazy val shutdownErrorEngineNotRunning: HeminException =
    new HeminException("Engine shutdown failed; reason: not running")

  private def shutdownError(ex: Throwable): HeminException =
    new HeminException(s"Engine shutdown failed; reason : ${ex.getMessage}", ex)

  private def breakerOpen(): Unit = log.info("Circuit Breaker is open")

  private def breakerClose(): Unit = log.warn("Circuit Breaker is closed")

  private def breakerHalfOpen(): Unit = log.info("Circuit Breaker is half-open, next message goes through")

  // TODO delete
  private def newExecutionContext: ExecutionContext = {
    /*
    ExecutionContext.fromExecutor(new ForkJoinPool(initialParallelism: Int))
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(limit: Int))
    */
    ExecutionContext.fromExecutor(new ForkJoinPool(4))
  }


}
