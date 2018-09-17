package exo.engine.crawler

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import exo.engine.EngineProtocol._

/**
  * @author Maximilian Irro
  */
class CrawlerPriorityActorMailbox (settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
        case ActorRefParserActor(_)        => 0
        case ActorRefCatalogStoreActor(_)  => 0
        case DownloadWithHeadCheck(_,_,_)  => 1
        case DownloadContent(_,_,_,_)      => 1
        case CrawlFyyd(_)                  => 1
        case _                             => 2
    })
