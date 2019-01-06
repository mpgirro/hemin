package io.hemin.engine

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import io.hemin.engine.catalog.CatalogStore._
import io.hemin.engine.model._
import io.hemin.engine.node.Node
import io.hemin.engine.node.Node.{CliInput, CliOutput, EngineOperational, StartupStatus}
import io.hemin.engine.searcher.Searcher.{SearchRequest, SearchResults}
import io.hemin.engine.util.mapper.MapperErrors

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object Engine {

  /** The name of the Engine. This value is used for configuration
    * namespace(s), as well as the default Mongo database name,
    * and Solr index name. */
  final val name: String = "hemin"

  /** Try to boot an [[io.hemin.engine.Engine]] instance for the given
    * configuration map. The configuration property map will be the
    * foundation of the internal [[io.hemin.engine.EngineConfig]]. Not
    * specified parameters will have default values. The configuration
    * map will be also tried for the internal Akka system configuration,
    * with fallbacks from [[io.hemin.engine.EngineConfig.defaultAkkaConfig]].
    *
    * @param config The configuration map that is the base for the Engine's
    *               configuration and the internal Akka system.
    */
  def boot(config: Config): Try[Engine] = Try(new Engine(config))

  /** Try to boot an [[io.hemin.engine.Engine]] instance for the given
    * configuration. The internal Akka system will use the default configuration
    * as it is defined in [[io.hemin.engine.EngineConfig.defaultAkkaConfig]].
    *
    * @param config The Engine's configuration. The Akka system will use defaults.
    */
  def boot(config: EngineConfig): Try[Engine] = Try(new Engine(config))

  private lazy val engineShutdownFailureNotRunning: Try[Unit] =
    Failure(new EngineException("Engine shutdown failed; reason: not running"))

  private lazy val engineGuardErrorNotRunning: EngineException =
    new EngineException("Guard prevented call; reason: Engine not running")

  private def engineGuardFailureNotRunning[A]: Try[A] = Failure(engineGuardErrorNotRunning)

  private def engineShutdownFailure(ex: Throwable): Try[Unit] = Failure(engineShutdownError(ex))

  private def engineShutdownError(ex: Throwable): EngineException =
    new EngineException(s"Engine shutdown failed; reason: ${ex.getMessage}", ex)

}

class Engine private (engineConfig: EngineConfig, akkaConfig: Config) {

  import io.hemin.engine.Engine._ // import the failures

  private def this(config: Config) = this(
    engineConfig = EngineConfig.load(config),
    akkaConfig   = Option(config)
      .getOrElse(ConfigFactory.empty())
      .withFallback(EngineConfig.defaultAkkaConfig),
  )

  private def this(config: EngineConfig) = this(
    engineConfig = Option(config).getOrElse(EngineConfig.defaultEngineConfig),
    akkaConfig   = EngineConfig.defaultAkkaConfig,
  )

  /** Configuration of the Engine instance. */
  val config: EngineConfig = engineConfig

  private val log = Logger(getClass)

  private lazy val running: AtomicBoolean = new AtomicBoolean(false)

  private implicit val internalTimeout: Timeout = config.node.internalTimeout
  private implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(4)) //  TODO set parameters from config

  private val system: ActorSystem = ActorSystem(Engine.name, akkaConfig)
  private val node: ActorRef = system.actorOf(Node.props(config), Node.name)

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


  // Run the boot sequence. This will throw an exception in case a Failure occurred
  bootSequence() match {
    case Success(_)  => log.info("ENGINE startup complete ...")
    case Failure(ex) => throw ex // escalate the construction fault, the factory method will wrap it in a Try-Failure
  }


  /** Attempts to shutdown the Engine. This operation is thread-safe.
    * It will produce a `Failure` if the Engine is not running. */
  def shutdown(): Try[Unit] = synchronized {
    if (running.get) {
      log.info("ENGINE is shutting down ...")
      //bus ! ShutdownSystem // TODO does system.terminate() work better?
      running.set(false) // we always set running to false, because on error the engine is still screwed up
      val terminate = Await.result(system.terminate(), internalTimeout.duration)
      Try(terminate) match {
        case Success(_)  => Success(Unit)
        case Failure(ex) => engineShutdownFailure(ex)
      }
    } else {
      engineShutdownFailureNotRunning
    }
  }

  /** Proposes a feed's URL to the system, which will
    * process it if the URL is yet unknown to the database. */
  def propose(url: String): Try[Unit] = guarded {
    bus ! ProposeNewFeed(url)
  }

  /** Eventually returns the data resulting from processing the
    * arguments by the Command Language Interpreter as text */
  def cli(args: String): Future[String] = guarded {
    (bus ? CliInput(args))
      .mapTo[CliOutput]
      .map(_.output)
  }

  /** Search the index for the given query parameter. Returns a
    * [[io.hemin.engine.model.SearchResult]] instance matching the
    * page and size parameters.
    *
    * @param query The query to search the internal reverse index for.
    * @param page  The page for the [[io.hemin.engine.model.SearchResult]]. If None, then
    *              [[io.hemin.engine.searcher.SearcherConfig.defaultPage]] is used.
    * @param size  The size (= maximum number of elements in the
    *              [[io.hemin.engine.model.SearchResult.results]] list) of the
    *              [[io.hemin.engine.model.SearchResult]]. If None, then
    *              [[io.hemin.engine.searcher.SearcherConfig.defaultSize]] is used.
    * @return The [[io.hemin.engine.model.SearchResult]] matching the query/page/size parameters.
    */
  def search(query: String, page: Option[Int], size: Option[Int]): Future[SearchResult] = guarded {
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

  /** Finds an [[io.hemin.engine.model.Episode]] by ID */
  def findEpisode(id: String): Future[Option[Episode]] = guarded {
    (bus ? GetEpisode(id))
      .mapTo[EpisodeResult]
      .map(_.episode)
  }

  /** Finds a [[io.hemin.engine.model.Feed]] by ID */
  def findFeed(id: String): Future[Option[Feed]] = guarded {
    (bus ? GetFeed(id))
      .mapTo[FeedResult]
      .map(_.feed)
  }

  /** Finds an [[io.hemin.engine.model.Image]] by ID */
  def findImage(id: String): Future[Option[Image]] = guarded {
    (bus ? GetImage(id))
      .mapTo[ImageResult]
      .map(_.image)
  }

  /*
  def findImageByPodcast(id: String): Future[Option[Image]] = guarded {
    (bus ? GetImageByPodcast(id))
      .mapTo[ImageResult]
      .map(_.image)
  }

  def findImageByEpisode(id: String): Future[Option[Image]] = guarded {
    (bus ? GetImageByEpisode(id))
      .mapTo[ImageResult]
      .map(_.image)
  }
  */

  /** Finds a slice of all [[io.hemin.engine.model.Podcast]] starting
    * from (`page` * `size`) and with `size` elements.
    *
    * @param page
    * @param size
    * @return
    */
  def findAllPodcasts(page: Option[Int], size: Option[Int]): Future[List[Podcast]] = guarded {
    (bus ? GetAllPodcastsRegistrationComplete(page, size))
      .mapTo[AllPodcastsResult]
      .map(_.podcasts)
  }

  /** Finds an [[io.hemin.engine.model.Episode]] by its belonging Podcast's ID */
  def findEpisodesByPodcast(id: String): Future[List[Episode]] = guarded {
    (bus ? GetEpisodesByPodcast(id))
      .mapTo[EpisodesByPodcastResult]
      .map(_.episodes)
  }

  /** Finds an [[io.hemin.engine.model.Feed]] by its belonging Podcast's ID */
  def findFeedsByPodcast(id: String): Future[List[Feed]] = guarded {
    (bus ? GetFeedsByPodcast(id))
      .mapTo[FeedsByPodcastResult]
      .map(_.feeds)
  }

  // TODO unused and deprecated, since Chapters are embedded directly into Episode's
  /** Finds all [[io.hemin.engine.model.Chapter]] by their belonging Episode's ID */
  def findChaptersByEpisode(id: String): Future[List[Chapter]] = guarded {
    (bus ? GetChaptersByEpisode(id))
      .mapTo[ChaptersByEpisodeResult]
      .map(_.chapters)
  }

  def findNewestPodcasts(pageNumber: Option[Int], pageSize: Option[Int]): Future[List[Podcast]] = guarded {
    (bus ? GetNewestPodcasts(pageNumber, pageSize))
      .mapTo[NewestPodcastsResult]
      .map(_.podcasts)
  }

  def findLatestEpisodes(pageNumber: Option[Int], pageSize: Option[Int]): Future[List[Episode]] = guarded {
    (bus ? GetLatestEpisodes(pageNumber, pageSize))
      .mapTo[LatestEpisodesResult]
      .map(_.episodes)
  }

  // The call to warmup() will tap the lazy values, and wait until all
  // subsystems in the actor hierarchy report that they are up and running
  private def bootSequence(): Try[Unit] = {
    log.info("ENGINE is starting up ...")
    warmup()
  }

  private def warmup(): Try[Unit] = {
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

  // TODO at some point I want to change this to something that distributes a message to the cluster
  private def bus: ActorRef = node

  private def guarded[T](body: => Future[T]): Future[T] =
    if (running.get) {
      circuitBreaker.withCircuitBreaker(body)
    } else {
      Future.failed(engineGuardErrorNotRunning)
    }

  private def guarded[T](body: => T): Try[T] =
    if (running.get) {
      Try(circuitBreaker.withSyncCircuitBreaker(body))
    } else {
      engineGuardFailureNotRunning[T]
    }

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
