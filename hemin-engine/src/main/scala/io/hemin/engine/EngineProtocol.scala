package io.hemin.engine

import akka.actor.ActorRef

object EngineProtocol {

  // These messages are sent to propagate actorRefs to other actors, to overcome circular dependencies
  trait ActorRefInfo

  final case class ActorRefCatalogStoreActor(ref: ActorRef) extends ActorRefInfo
  final case class ActorRefCrawlerActor(ref: ActorRef) extends ActorRefInfo
  final case class ActorRefParserActor(ref: ActorRef) extends ActorRefInfo
  final case class ActorRefIndexStoreActor(ref: ActorRef) extends ActorRefInfo
  final case class ActorRefUpdaterActor(ref: ActorRef) extends ActorRefInfo
  final case class ActorRefSupervisor(ref: ActorRef) extends ActorRefInfo

  // Startup sequence messages
  final case class ReportCatalogStoreStartupComplete()
  final case class ReportIndexStoreStartupComplete()
  final case class ReportCrawlerStartupComplete()
  final case class ReportParserStartupComplete()
  final case class ReportSearcherStartupComplete()
  final case class ReportUpdaterStartupComplete()
  final case class ReportWorkerStartupComplete() // for worker/handler delegation children

  final case class EngineOperational()
  final case class StartupComplete()
  final case class StartupInProgress()

  // These are maintenance methods, I use during development
  final case class DebugPrintAllPodcasts()    // User/CLI -> CatalogStore
  final case class DebugPrintAllEpisodes()    // User/CLI -> CatalogStore
  final case class DebugPrintAllFeeds()
  //case class DebugPrintCountAllPodcasts()
  //case class DebugPrintCountAllEpisodes()
  //case class DebugPrintCountAllFeeds()
  final case class LoadTestFeeds()            // CLI -> CatalogStore
  final case class LoadMassiveFeeds()         // CLI -> CatalogStore

  // User -> Crawler
  // TODO: automatic: Crawler -> Crawler on a regular basis
  trait CrawlExternalDirectory
  final case class CrawlFyyd(count: Int) extends CrawlExternalDirectory
  final case class LoadFyydEpisodes(podcastId: String, fyydId: Long) extends CrawlExternalDirectory

  // CLI -> Master
  final case class ShutdownSystem()

}
