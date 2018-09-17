package exo.engine

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import exo.engine.EngineProtocol.ShutdownSystem
import exo.engine.catalog.CatalogStore._
import exo.engine.domain.dto._
import exo.engine.index.IndexStore.{SearchIndex, SearchResults}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * @author max
  */
class ExoEngine {

    private val CONFIG = ConfigFactory.load()
    private implicit val INTERNAL_TIMEOUT: Timeout = Option(CONFIG.getInt("echo.internal-timeout")).getOrElse(5).seconds
    private val DEFAULT_PAGE: Int = Option(CONFIG.getInt("echo.gateway.default-page")).getOrElse(1)
    private val DEFAULT_SIZE: Int = Option(CONFIG.getInt("echo.gateway.default-size")).getOrElse(20)

    private val BREAKER_CALL_TIMEOUT: FiniteDuration = Option(CONFIG.getInt("echo.gateway.breaker-call-timeout")).getOrElse(5).seconds
    private val BREAKER_RESET_TIMEOUT: FiniteDuration = Option(CONFIG.getInt("echo.gateway.breaker-reset-timeout")).getOrElse(10).seconds
    private val MAX_BREAKER_FAILURES: Int = 2 // TODO read from config

    private val log = Logger(classOf[ExoEngine])

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
        val config = ConfigFactory.load
        val system = ActorSystem("exo", config)
        master = system.actorOf(Props(new NodeMaster), NodeMaster.name)
    }

    def shutdown(): Unit = {
        master ! ShutdownSystem
    }

    def bus(): ActorRef = master

    def propose(url: String): Unit = {
        bus ! ProposeNewFeed(url)
    }

    def search(query: String, page: Int, size: Int): Future[ResultWrapper] = {
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
        val p: Int = page.map(p => p-1).getOrElse(DEFAULT_PAGE)
        val s: Int = size.getOrElse(DEFAULT_SIZE)

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
