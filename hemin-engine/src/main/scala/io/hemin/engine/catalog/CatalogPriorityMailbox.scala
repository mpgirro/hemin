package io.hemin.engine.catalog

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import io.hemin.engine.EngineProtocol._
import io.hemin.engine.Node.{ActorRefCrawlerActor, DebugPrintAllEpisodes, DebugPrintAllFeeds, DebugPrintAllPodcasts}
import io.hemin.engine.catalog.CatalogStore._

/** Priority mailbox for [[io.hemin.engine.catalog.CatalogStore]] */
class CatalogPriorityMailbox(settings: ActorSystem.Settings, config: Config)
  extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
      case ActorRefCrawlerActor(_)    => 0
      case DebugPrintAllPodcasts      => 0
      case DebugPrintAllEpisodes      => 0
      case DebugPrintAllFeeds         => 0
      //case DebugPrintCountAllPodcasts => 0
      //case DebugPrintCountAllEpisodes => 0
      //case DebugPrintCountAllFeeds    => 0
      case GetPodcast(_)              => 1
      case GetAllPodcasts             => 1
      case GetEpisode(_)              => 1
      case GetEpisodesByPodcast(_)    => 1
      case FeedStatusUpdate(_,_,_,_)  => 2
      case UpdatePodcast(_,_,_)       => 3
      case ProposeNewFeed(_)          => 4
      case CheckPodcast(_)            => 5
      case CheckFeed(_)               => 5
      //case CheckAllPodcasts           => 5
      case CheckAllFeeds              => 5
      case _                          => 6
    })
