package io.hemin.engine

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import io.hemin.engine.EngineProtocol.{EngineOperational, ShutdownSystem, StartupComplete, StartupInProgress}
import io.hemin.engine.NodeMaster.{CliInput, CliOutput}
import io.hemin.engine.catalog.CatalogStore._
import io.hemin.engine.domain._
import io.hemin.engine.searcher.Searcher.{SearcherRequest, SearcherResults}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

class Engine {

  private var engineConfig: EngineConfig = _
  private implicit var internalTimeout: Timeout = _

  private val log = Logger(classOf[Engine])

  private var master: ActorRef = _

  private implicit val ec: ExecutionContext = ExecutionContext.global // TODO anderen als global EC

  /* TODO ich will einen CircuitBreaker, habe aber keinen Scheduler weil das hier kein Actor ist
  private val indexBreaker =
      CircuitBreaker(context.system.scheduler, MAX_BREAKER_FAILURES, BREAKER_CALL_TIMEOUT, BREAKER_RESET_TIMEOUT)
          .onOpen(breakerOpen("Index"))
          .onClose(breakerClose("Index"))
          .onHalfOpen(breakerHalfOpen("Index"))
  */

  def start(): Unit = {
    // load and init the configuration
    val globalConfig = ConfigFactory.load(System.getProperty("config.resource", "application.conf"))
    engineConfig = EngineConfig.load(globalConfig)
    internalTimeout = engineConfig.internalTimeout

    // init the actorsystem and local master for this node
    val system = ActorSystem("hemin", globalConfig)
    master = system.actorOf(Props(new NodeMaster(engineConfig)), NodeMaster.name)

    // wait until all actors in the hierarchy report they are up and running
    var warmup = true
    while (warmup) {
      val request = bus ? EngineOperational
      val response = Await.result(request, 10.seconds) // TODO read timeout from config
      response match {
        case StartupComplete   =>
          warmup = false
        case StartupInProgress =>
          warmup = true
          Thread.sleep(100)
      }
    }

    log.info("engine is up and running")
  }

  def shutdown(): Unit = bus ! ShutdownSystem

  def bus: ActorRef = master

  def config: EngineConfig = engineConfig

  def cli(args: String): Future[String] =
    (bus ? CliInput(args)).map {
      case CliOutput(txt) => txt
    }

  def propose(url: String): Unit = bus ! ProposeNewFeed(url)

  def search(query: String, page: Option[Int], size: Option[Int]): Future[ResultsWrapper] = {
    val p: Int = page.getOrElse(engineConfig.indexConfig.defaultPage)
    val s: Int = size.getOrElse(engineConfig.indexConfig.defaultSize)

    search(query, p, s)
  }

  def search(query: String, page: Int, size: Int): Future[ResultsWrapper] =
    (bus ? SearcherRequest(query, page, size)).map {
      case SearcherResults(rs) => rs
    }

  def findPodcast(id: String): Future[Option[Podcast]] =
    (bus ? GetPodcast(id)).map {
      case PodcastResult(p) => p
    }

  def findEpisode(id: String): Future[Option[Episode]] =
    (bus ? GetEpisode(id)).map {
      case EpisodeResult(e) => e
    }

  def findFeed(id: String): Future[Option[Feed]] =
    (bus ? GetFeed(id)).map {
      case FeedResult(f) => f
    }

  def findAllPodcasts(page: Option[Int], size: Option[Int]): Future[List[Podcast]] = {
    val p: Int = page.getOrElse(engineConfig.catalogConfig.defaultPage) - 1
    val s: Int = size.getOrElse(engineConfig.catalogConfig.defaultSize)

    (bus ? GetAllPodcastsRegistrationComplete(p,s)).map {
      case AllPodcastsResult(ps) => ps
    }
  }

  def findEpisodesByPodcast(id: String): Future[List[Episode]] =
    (bus ? GetEpisodesByPodcast(id)).map {
      case EpisodesByPodcastResult(es) => es
    }

  def findFeedsByPodcast(id: String): Future[List[Feed]] =
    (bus ? GetFeedsByPodcast(id)).map {
      case FeedsByPodcastResult(fs) => fs
    }

  def findChaptersByEpisode(id: String): Future[List[Chapter]] =
    (bus ? GetChaptersByEpisode(id)).map {
      case ChaptersByEpisodeResult(cs) => cs
    }

  private def breakerOpen(name: String): Unit = {
    log.warn("{} Circuit Breaker is open", name)
  }

  private def breakerClose(name: String): Unit = {
    log.warn("{} Circuit Breaker is closed", name)
  }

  private def breakerHalfOpen(name: String): Unit = {
    log.warn("{} Circuit Breaker is half-open, next message goes through", name)
  }

}