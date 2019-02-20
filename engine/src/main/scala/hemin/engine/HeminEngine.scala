package hemin.engine

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import hemin.engine.HeminEngine._
import hemin.engine.model._
import hemin.engine.node.Node
import hemin.engine.util.GuardedOperationDispatcher

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object HeminEngine {

  /** The name of the Engine. This value is used for configuration
    * namespace(s), as well as the default Mongo database name,
    * and Solr index name. */
  final val name: String = "hemin"

  final val version: String = "0.10.0"

  /** Try to boot an [[hemin.engine.HeminEngine]] instance for the given
    * configuration map. The configuration property map will be the
    * foundation of the internal [[hemin.engine.HeminConfig]]. Not
    * specified parameters will have default values. The configuration
    * map will be also tried for the internal Akka system configuration,
    * with fallbacks from [[hemin.engine.HeminConfig.defaultAkkaConfig]].
    *
    * @param config The configuration map that is the base for the Engine's
    *               configuration and the internal Akka system.
    */
  def boot(config: Config): Try[HeminEngine] = Try(new HeminEngine(config))

  /** Try to boot an [[hemin.engine.HeminEngine]] instance for the given
    * configuration. The internal Akka system will use the default configuration
    * as it is defined in [[hemin.engine.HeminConfig.defaultAkkaConfig]].
    *
    * @param config The Engine's configuration. The Akka system will use defaults.
    */
  def boot(config: HeminConfig): Try[HeminEngine] = Try(new HeminEngine(config))

  private lazy val engineShutdownFailureNotRunning: Try[Unit] =
    Failure(new HeminException("Engine shutdown failed; reason: not running"))

  private lazy val engineGuardErrorNotRunning: HeminException =
    new HeminException("Guard prevented call; reason: Engine not running")

  private def engineGuardFailureNotRunning[A]: Try[A] = Failure(engineGuardErrorNotRunning)

  private def engineShutdownFailure(ex: Throwable): Try[Unit] = Failure(engineShutdownError(ex))

  private def engineShutdownError(ex: Throwable): HeminException =
    new HeminException(s"Engine shutdown failed; reason: ${ex.getMessage}", ex)

}

class HeminEngine private (engineConfig: HeminConfig,
                           akkaConfig: Config) { // import the failures

  private def this(config: Config) = this(
    engineConfig = HeminConfig.load(config),
    akkaConfig   = Option(config)
      .getOrElse(ConfigFactory.empty())
      .withFallback(HeminConfig.defaultAkkaConfig),
  )

  private def this(config: HeminConfig) = this(
    engineConfig = Option(config).getOrElse(HeminConfig.defaultEngineConfig),
    akkaConfig   = HeminConfig.defaultAkkaConfig,
  )

  /** Configuration of the Engine instance. */
  val config: HeminConfig = engineConfig

  private val log = Logger(getClass)

  private lazy val running: AtomicBoolean = new AtomicBoolean(false)

  private implicit val internalTimeout: Timeout = config.node.internalTimeout
  private implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(4)) //  TODO set parameters from config

  private val system: ActorSystem = ActorSystem(HeminEngine.name, akkaConfig)
  private val node: ActorRef = system.actorOf(Node.props(config), Node.name)
  private val guarded: GuardedOperationDispatcher = new GuardedOperationDispatcher(bus, system, config, executionContext)

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
  def propose(url: String): Try[Unit] = ifRunning {
    //bus ! CatalogStore.ProposeNewFeed(url)
    guarded.proposeFeed(url)
  }

  /** Eventually returns the data resulting from processing the
    * arguments by the Command Language Interpreter as text */
  def cli(input: String): Future[String] = ifRunning {
    guarded.cli(input)
  }

  /** Search the index for the given query parameter. Returns a
    * [[hemin.engine.model.SearchResult]] instance matching the
    * page and size parameters.
    *
    * @param query      The query to search the internal reverse index for.
    * @param pageNumber The page for the [[hemin.engine.model.SearchResult]]. If None, then
    *                   [[hemin.engine.searcher.SearcherConfig.defaultPage]] is used.
    * @param pageSize   The size (= maximum number of elements in the
    *                   [[hemin.engine.model.SearchResult.results]] list) of the
    *                   [[hemin.engine.model.SearchResult]]. If None, then
    *                   [[hemin.engine.searcher.SearcherConfig.defaultSize]] is used.
    * @return The [[hemin.engine.model.SearchResult]] matching the query/page/size parameters.
    */
  def search(query: String, pageNumber: Option[Int], pageSize: Option[Int]): Future[SearchResult] = ifRunning {
    guarded.getSearchResult(query, pageNumber, pageSize)
  }

  /** Finds a [[hemin.engine.model.Podcast]] by ID */
  def findPodcast(id: String): Future[Option[Podcast]] = ifRunning {
    guarded.getPodcast(id)
  }

  /** Finds an [[hemin.engine.model.Episode]] by ID */
  def findEpisode(id: String): Future[Option[Episode]] = ifRunning {
    guarded.getEpisode(id)
  }

  /** Finds a [[hemin.engine.model.Feed]] by ID */
  def findFeed(id: String): Future[Option[Feed]] = ifRunning {
    guarded.getFeeds(id)
  }

  /** Finds an [[hemin.engine.model.Image]] by ID */
  def findImage(id: String): Future[Option[Image]] = ifRunning {
    guarded.getImage(id)
  }

  /*
  def findImageByPodcast(id: String): Future[Option[Image]] = guarded {
    (bus ? CatalogStore.GetImageByPodcast(id))
      .mapTo[CatalogStore.ImageResult]
      .map(_.image)
  }

  def findImageByEpisode(id: String): Future[Option[Image]] = guarded {
    (bus ? CatalogStore.GetImageByEpisode(id))
      .mapTo[CatalogStore.ImageResult]
      .map(_.image)
  }
  */

  /** Finds a slice of all [[hemin.engine.model.Podcast]] starting
    * from (`page` * `size`) and with `size` elements.
    *
    * @param pageNumber
    * @param pageSize
    * @return
    */
  def findAllPodcasts(pageNumber: Option[Int], pageSize: Option[Int]): Future[List[Podcast]] = ifRunning {
    guarded.getAllPodcasts(pageNumber, pageSize)
  }

  /** Finds an [[hemin.engine.model.Episode]] by its belonging Podcast's ID */
  def findEpisodesByPodcast(id: String): Future[List[Episode]] = ifRunning {
    guarded.getPodcastEpisodes(id)
  }

  /** Finds an [[hemin.engine.model.Feed]] by its belonging Podcast's ID */
  def findFeedsByPodcast(id: String): Future[List[Feed]] = ifRunning {
    guarded.getPodcastFeeds(id)
  }

  /** Finds all [[hemin.engine.model.Chapter]] by their belonging Episode's ID */
  def findChaptersByEpisode(id: String): Future[List[Chapter]] = ifRunning {
    guarded.getEpisodeChapters(id)
  }

  def findNewestPodcasts(pageNumber: Option[Int], pageSize: Option[Int]): Future[List[Podcast]] = ifRunning {
    guarded.getAllPodcastsByNewest(pageNumber, pageSize)
  }

  def findLatestEpisodes(pageNumber: Option[Int], pageSize: Option[Int]): Future[List[Episode]] = ifRunning {
    guarded.getAllEpisodesByLatest(pageNumber, pageSize)
  }

  def getDatabaseStats: Future[DatabaseStats] = ifRunning {
    guarded.getDatabaseStats
  }

  def getDistinctCategories: Future[Set[String]] = ifRunning {
    guarded.getDistinctCategories
  }

  def opmlImport(xmlData: String): Try[Unit] = ifRunning {
    guarded.importOpml(xmlData)
  }

  // The call to warmup() will tap the lazy values, and wait until all
  // subsystems in the actor hierarchy report that they are up and running
  private def bootSequence(): Try[Unit] = {
    log.info("ENGINE is starting up ...")
    warmup()
  }

  private def warmup(): Try[Unit] = {
    val startup = bus ? Node.EngineOperational
    Await.result(startup, internalTimeout.duration) match {
      case Node.StartupStatus(true) =>
        running.set(true)
        Success(Unit)
      case Node.StartupStatus(false) =>
        Thread.sleep(25) // don't wait too busy
        warmup()
    }
  }

  // TODO at some point I want to change this to something that distributes a message to the cluster
  private def bus: ActorRef = node

  private def ifRunning[T](body: => Future[T]): Future[T] =
    if (running.get) {
      body
    } else {
      Future.failed(engineGuardErrorNotRunning)
    }

  private def ifRunning[T](body: => T): Try[T] =
    if (running.get) {
      Try(body)
    } else {
      engineGuardFailureNotRunning[T]
    }

}
