package io.disposia.engine

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import io.disposia.engine.EngineProtocol.{EngineOperational, ShutdownSystem, StartupComplete, StartupInProgress}
import io.disposia.engine.catalog.CatalogStore._
import io.disposia.engine.config.ExoConfig
import io.disposia.engine.domain.dto._
import io.disposia.engine.index.IndexStore.{SearchIndex, SearchResults}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

/**
  * @author max
  */
class DisposiaEngine {

    /*
    private val CONFIG = ConfigFactory.load()
    private implicit val INTERNAL_TIMEOUT: Timeout = Option(CONFIG.getInt("echo.internal-timeout")).getOrElse(5).seconds
    private val DEFAULT_PAGE: Int = Option(CONFIG.getInt("echo.gateway.default-page")).getOrElse(1)
    private val DEFAULT_SIZE: Int = Option(CONFIG.getInt("echo.gateway.default-size")).getOrElse(20)

    private val BREAKER_CALL_TIMEOUT: FiniteDuration = Option(CONFIG.getInt("echo.gateway.breaker-call-timeout")).getOrElse(5).seconds
    private val BREAKER_RESET_TIMEOUT: FiniteDuration = Option(CONFIG.getInt("echo.gateway.breaker-reset-timeout")).getOrElse(10).seconds
    private val MAX_BREAKER_FAILURES: Int = 2 // TODO read from config
    */

    private var config: ExoConfig = _
    private implicit var internalTimeout: Timeout = _

    private val log = Logger(classOf[DisposiaEngine])

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
        config = ExoConfig.load(globalConfig)
        internalTimeout = config.internalTimeout

        // init the actorsystem and local master for this node
        val system = ActorSystem("disposia", globalConfig)
        master = system.actorOf(Props(new NodeMaster(config)), NodeMaster.name)

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

    def shutdown(): Unit = {
        master ! ShutdownSystem
    }

    def bus(): ActorRef = master

    def propose(url: String): Unit = {
        bus ! ProposeNewFeed(url)
    }

    def search(query: String, page: Int, size: Int): Future[ResultWrapper] = {

        // TODO-1 die defaultPage und defaultSize in den Config für Catalog und Index vereinen!
        // TODO-2 vielleicht in ApplicationConfig?
        //val p: Int = page.map(p => p-1).getOrElse(config.catalogConfig.defaultPage)
        //val s: Int = size.getOrElse(config.catalogConfig.defaultSize)

        (bus ? SearchIndex(query, page, size)).map {
            case SearchResults(_, results) => results
        }
    }

    def findEpisode(exo: String): Future[Option[Episode]] = {
        (bus ? GetEpisode(exo)).map {
            case EpisodeResult(episode) => Some(episode)
            case NothingFound(unknown)  => None
        }
    }

    def findChaptersByEpisode(exo: String): Future[List[Chapter]] = {
        (bus ? GetChaptersByEpisode(exo)).map {
            case ChaptersByEpisodeResult(chapters) => chapters
        }
    }

    def findAllPodcasts(page: Option[Int], size: Option[Int]): Future[List[Podcast]] = {
        val p: Int = page.getOrElse(config.catalogConfig.defaultPage)
        val s: Int = size.getOrElse(config.catalogConfig.defaultSize)

        (bus ? GetAllPodcastsRegistrationComplete(p,s)).map {
            case AllPodcastsResult(podcasts) => podcasts
        }
    }

    def findPodcast(exo: String): Future[Option[Podcast]] = {
        (bus ? GetPodcast(exo)).map {
            case PodcastResult(podcast) => Some(podcast)
            case NothingFound(unknown)  => None
        }
    }

    def findEpisodesByPodcast(exo: String): Future[List[Episode]] = {
        (bus ? GetEpisodesByPodcast(exo)).map {
            case EpisodesByPodcastResult(episodes) => episodes
        }
    }

    def findFeedsByPodcast(exo: String): Future[List[Feed]] = {
        (bus ? GetFeedsByPodcast(exo)).map {
            case FeedsByPodcastResult(feeds) => feeds
        }
    }

    def findFeed(exo: String): Future[Option[Feed]] = {
        (bus ? GetFeed(exo)).map {
            case FeedResult(feed) => Some(feed)
            case NothingFound(unknown)  => None
        }
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
