package io.hemin.engine

import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import io.hemin.engine.EngineProtocol._
import io.hemin.engine.Node.{CliInput, CliOutput}
import io.hemin.engine.catalog.CatalogStore._
import io.hemin.engine.model._
import io.hemin.engine.exception.HeminException
import io.hemin.engine.searcher.Searcher.{SearchRequest, SearchResults}
import io.hemin.engine.util.cli.CliProcessor

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{Await, ExecutionContext, Future, blocking}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object Engine {
  final val name: String = "hemin"
}

class Engine (private val initConfig: Config) {

  private val log = Logger(getClass)

  private lazy val running: AtomicBoolean = new AtomicBoolean(false)

  private lazy val completeConfig: Config = initConfig.withFallback(EngineConfig.defaultConfig)
  private lazy val engineConfig: EngineConfig = EngineConfig.load(completeConfig)

  private implicit lazy val internalTimeout: Timeout = engineConfig.node.internalTimeout
  //private implicit lazy val executionContext: ExecutionContext = system.dispatchers.lookup(engineConfig.node.dispatcher)
  //private implicit lazy val executionContext: ExecutionContext = ExecutionContext.global // TODO
  private implicit val executionContext: ExecutionContext = newExecutionContext // TODO

  // lazy init the actor system and local bus for this node
  private[engine] val system: ActorSystem = ActorSystem(Engine.name, completeConfig)
  private[engine] val node: ActorRef = system.actorOf(Props(new Node(engineConfig)), Node.name)

  private lazy val circuitBreaker: CircuitBreaker =
    (for {
      scheduler    <- Option(system).map(_.scheduler)
      maxFailures  <- Option(engineConfig).map(_.node.breakerMaxFailures)
      callTimeout  <- Option(engineConfig).map(_.node.breakerCallTimeout.duration)
      resetTimeout <- Option(engineConfig).map(_.node.breakerResetTimeout.duration)
    } yield CircuitBreaker(scheduler, maxFailures, callTimeout, resetTimeout)
      .onOpen(breakerOpen())
      .onClose(breakerClose())
      .onHalfOpen(breakerHalfOpen())).get


  // Run the startup sequence. This will throw an exception in case a Failure occurred
  startupSequence() match {
    case Success(_)  => log.info("ENGINE startup complete ...")
    case Failure(ex) => throw ex
  }


  /** Attempts to shutdown the Engine. This operation is thread-safe.
    * It will produce a `Failure` if the Engine is not running. */
  def shutdown(): Try[Unit] = synchronized { if (running.get) shutdownOnWarm() else shutdownOnCold() }

  /** Configuration of the Engine instance */
  def config: EngineConfig = engineConfig

  def propose(url: String): Try[Unit] = guarded {
    bus ! ProposeNewFeed(url)
  }

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
  private def startupSequence(): Try[Unit] = synchronized {
    log.info("ENGINE is starting up ...")
    warmup()
    //warmup3(dispatchStartupStatusCheck)
  }

  /*
  private def warmup2(): Future[String] = {
    (bus ? EngineOperational)
      .andThen {
        case Success(x)  => x match {
          case StartupComplete =>
            running.set(true)
            Future.successful[String]("ENGINE startup complete ...")
          case StartupInProgress =>
            Thread.sleep(25) // don't wait too busy
            warmup2()
        }
        case Failure(ex) => Future.failed[String](ex)
      }
  }
  */

  /*
  private def warmup3(status: Future[StartupStatus]): Try[Unit] = {
    if (status.isCompleted) {
      Await.result(status, internalTimeout.duration) match {
        case StartupStatus(true) =>
          running.set(true)
          Success(Unit)
        case StartupStatus(false) =>
          warmup3(dispatchStartupStatusCheck)
      }
    } else {
      Thread.`yield`() // TODO experimental; somehow we need to ensure that the Node actor makes progress...
      Thread.sleep(50) // don't wait too busy
      warmup3(status)
    }
  }

  private def dispatchStartupStatusCheck: Future[StartupStatus] = (bus ? EngineOperational).mapTo[StartupStatus]
  */


  private def warmup(): Try[Unit] = blocking {
    val startup = bus ? EngineOperational
    //Thread.`yield`() // TODO experimental
    Thread.sleep(100) // don't wait too busy
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

  private def newExecutionContext: ExecutionContext = {
    /*
    ExecutionContext.fromExecutor(new ForkJoinPool(initialParallelism: Int))
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(limit: Int))
    */
    ExecutionContext.fromExecutor(new ForkJoinPool(4))
  }


}
