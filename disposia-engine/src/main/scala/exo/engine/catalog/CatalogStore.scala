package exo.engine.catalog

import java.sql.{Connection, DriverManager}
import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import exo.engine.EngineProtocol._
import exo.engine.config.CatalogConfig
import exo.engine.domain.FeedStatus
import exo.engine.domain.dto.{Chapter, Episode, Feed, Podcast}
import liquibase.{Contexts, LabelExpression, Liquibase}
import liquibase.database.{Database, DatabaseFactory}
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Maximilian Irro
  */

object CatalogStore {
    //def name(storeIndex: Int): String = "store-" + storeIndex
    final val name = "catalog"
    def props(config: CatalogConfig): Props = {
        Props(new CatalogStore(config)).withDispatcher("echo.catalog.dispatcher")
    }

    trait CatalogMessage
    trait CatalogEvent extends CatalogMessage
    trait CatalogCommand extends CatalogMessage
    trait CatalogQuery extends CatalogMessage
    trait CatalogQueryResult extends CatalogMessage
    // CatalogCommands
    case class ProposeNewFeed(url: String) extends CatalogCommand                 // Web/CLI -> CatalogStore
    case class RegisterEpisodeIfNew(podcastExo: String, episode: Episode) extends CatalogCommand // Questions: Parser -> CatalogStore
    // CatalogEvents
    case class AddPodcastAndFeedIfUnknown(podcast: Podcast, feed: Feed) extends CatalogEvent
    case class FeedStatusUpdate(podcastExo: String, feedUrl: String, timestamp: LocalDateTime, status: FeedStatus) extends CatalogEvent
    case class UpdateFeedUrl(oldUrl: String, newUrl: String) extends CatalogEvent
    case class UpdateLinkByExo(exo: String, newUrl: String) extends CatalogEvent
    case class SaveChapter(chapter: Chapter) extends CatalogEvent
    case class UpdatePodcast(podcastExo: String, feedUrl: String, podcast: Podcast) extends CatalogEvent
    case class UpdateEpisode(podcastExo: String, episode: Episode) extends CatalogEvent
    case class UpdateEpisodeWithChapters(podcastExo: String, episode: Episode, chapter: List[Chapter]) extends CatalogEvent
    // CatalogQueries
    case class GetPodcast(podcastExo: String) extends CatalogQuery
    case class GetAllPodcasts(page: Int, size: Int) extends CatalogQuery
    case class GetAllPodcastsRegistrationComplete(page: Int, size: Int) extends CatalogQuery
    case class GetAllFeeds(page: Int, size: Int) extends CatalogQuery
    case class GetEpisode(episodeExo: String) extends CatalogQuery
    case class GetEpisodesByPodcast(podcastExo: String) extends CatalogQuery
    case class GetFeedsByPodcast(podcastExo: String) extends CatalogQuery
    case class GetChaptersByEpisode(episodeExo: String) extends CatalogQuery
    case class CheckPodcast(exo: String) extends CatalogQuery
    case class CheckFeed(exo: String) extends CatalogQuery
    case class CheckAllPodcasts() extends CatalogQuery
    case class CheckAllFeeds() extends CatalogQuery
    // CatalogQueryResults
    case class PodcastResult(podcast: Podcast) extends CatalogQueryResult
    case class AllPodcastsResult(results: List[Podcast]) extends CatalogQueryResult
    case class AllFeedsResult(results: List[Feed]) extends CatalogQueryResult
    case class EpisodeResult(episode: Episode) extends CatalogQueryResult                      // TODO make it an option, and remove NothingFound message
    case class EpisodesByPodcastResult(episodes: List[Episode]) extends CatalogQueryResult
    case class FeedsByPodcastResult(feeds: List[Feed]) extends CatalogQueryResult
    case class ChaptersByEpisodeResult(chapters: List[Chapter]) extends CatalogQueryResult
    case class NothingFound(exo: String) extends CatalogQueryResult
}

class CatalogStore(config: CatalogConfig) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    /*
    private val CONFIG = ConfigFactory.load()
    private val WORKER_COUNT: Int = Option(CONFIG.getInt("echo.catalog.worker-count")).getOrElse(5)
    */

    private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.catalog.dispatcher")

    private var currentWorkerIndex = 0

    private var catalogStore: ActorRef = _
    private var indexStore: ActorRef = _
    private var crawler: ActorRef = _
    private var updater: ActorRef = _
    private var supervisor: ActorRef = _

    private var workerReportedStartupFinished = 0
    private var router: Router = {
        val routees = Vector.fill(config.workerCount) {
            val catalogStore = createCatalogStoreWorkerActor(config.databaseUrl)
            context watch catalogStore
            ActorRefRoutee(catalogStore)
        }
        Router(RoundRobinRoutingLogic(), routees)
    }

    override def preStart(): Unit = {
        Future {
            runLiquibaseUpdate()
        }
    }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {

        case msg @ ActorRefCatalogStoreActor(ref) =>
            log.debug("Received ActorRefCatalogStoreActor(_)")
            catalogStore = ref
            router.routees.foreach(r => r.send(msg, sender()))

        case msg @ ActorRefIndexStoreActor(ref) =>
            log.debug("Received ActorRefIndexStoreActor(_)")
            indexStore = ref
            router.routees.foreach(r => r.send(msg, sender()))

        case msg @ ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref
            router.routees.foreach(r => r.send(msg, sender()))

        case msg @ ActorRefUpdaterActor(ref) =>
            log.debug("Received ActorRefUpdaterActor(_)")
            updater = ref
            router.routees.foreach(r => r.send(msg, sender()))

        case ActorRefSupervisor(ref) =>
            log.debug("Received ActorRefSupervisor(_)")
            supervisor = ref
            reportStartupCompleteIfViable()

        case ReportWorkerStartupComplete =>
            workerReportedStartupFinished += 1
            reportStartupCompleteIfViable()

        case Terminated(corpse) =>
            log.error(s"A ${self.path} worker died : {}", corpse.path.name)
            context.stop(self)

        case PoisonPill =>
            log.debug("Received a PosionPill -> forwarding it to all routees")
            router.routees.foreach(r => r.send(PoisonPill, sender()))

        case work =>
            log.debug("Routing work of kind : {}", work.getClass)
            router.route(work, sender())

    }

    private def reportStartupCompleteIfViable(): Unit = {
        if (workerReportedStartupFinished == config.workerCount && supervisor != null) {
            supervisor ! ReportCatalogStoreStartupComplete
        }
    }

    private def createCatalogStoreWorkerActor(databaseUrl: String): ActorRef = {
        currentWorkerIndex += 1
        val workerIndex = currentWorkerIndex
        val catalogStore = context.actorOf(CatalogStoreHandler.props(workerIndex, config), CatalogStoreHandler.name(workerIndex))

        // forward the actor refs to the worker, but only if those references haven't died
        Option(crawler).foreach(c => catalogStore ! ActorRefCrawlerActor(c) )
        catalogStore ! ActorRefSupervisor(self)

        catalogStore
    }

    private def runLiquibaseUpdate(): Unit = {
        val startTime = System.currentTimeMillis
        try {
            Class.forName("org.h2.Driver")
            val conn: Connection = DriverManager.getConnection(
                s"${config.databaseUrl};DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                "sa",
                "")

            val database: Database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn))
            //database.setDefaultSchemaName("echo")

            val liquibase: Liquibase = new Liquibase("liquibase/master.xml", new ClassLoaderResourceAccessor(), database)

            val isDropFirst = true // TODO set this as a parameter
            if (isDropFirst) {
                liquibase.dropAll()
            }

            if (liquibase.isSafeToRunUpdate) {
                liquibase.update(new Contexts(), new LabelExpression())
            } else {
                log.warning("Liquibase reports it is NOT safe to run the update")
            }
        } catch {
            case e: Exception => log.error("Error on Liquibase update: {}", e)
        } finally {
            val stopTime = System.currentTimeMillis
            val elapsedTime = stopTime - startTime
            log.info("Run Liquibase in {} ms", elapsedTime)
        }
    }
}
