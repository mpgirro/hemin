package exo.engine

import akka.actor.ActorRef
import com.google.common.collect.ImmutableList
import exo.engine.domain.dto._


/**
  * @author Maximilian Irro
  */
object EngineProtocol {

    // Gateway(= Web) -> Searcher; CLI -> Gateway (Benchmark)
    case class SearchRequest(query: String, page: Option[Int], size: Option[Int])

    // These messages are sent to propagate actorRefs to other actors, to overcome circular dependencies
    trait ActorRefInfo

    case class ActorRefCatalogStoreActor(ref: ActorRef) extends ActorRefInfo
    case class ActorRefCrawlerActor(ref: ActorRef) extends ActorRefInfo
    case class ActorRefParserActor(ref: ActorRef) extends ActorRefInfo
    case class ActorRefIndexStoreActor(ref: ActorRef) extends ActorRefInfo
    case class ActorRefUpdaterActor(ref: ActorRef) extends ActorRefInfo
    case class ActorRefSupervisor(ref: ActorRef) extends ActorRefInfo

    // Startup sequence messages
    case class ReportCatalogStoreStartupComplete()
    case class ReportIndexStoreStartupComplete()
    case class ReportCrawlerStartupComplete()
    case class ReportParserStartupComplete()
    case class ReportUpdaterStartupComplete()
    case class ReportWorkerStartupComplete() // for worker/handler delegation children

    case class EngineOperational()
    case class StartupComplete()
    case class StartupInProgress()

    // These are maintenance methods, I use during development
    case class DebugPrintAllPodcasts()    // User/CLI -> CatalogStore
    case class DebugPrintAllEpisodes()    // User/CLI -> CatalogStore
    case class DebugPrintAllFeeds()
    case class DebugPrintCountAllPodcasts()
    case class DebugPrintCountAllEpisodes()
    case class DebugPrintCountAllFeeds()
    case class LoadTestFeeds()            // CLI -> CatalogStore
    case class LoadMassiveFeeds()         // CLI -> CatalogStore

    // User -> Crawler
    // TODO: automatic: Crawler -> Crawler on a regular basis
    trait CrawlExternalDirectory
    case class CrawlFyyd(count: Int) extends CrawlExternalDirectory
    case class LoadFyydEpisodes(podcastId: String, fyydId: Long) extends CrawlExternalDirectory

    // CLI -> Master
    case class ShutdownSystem()

}
