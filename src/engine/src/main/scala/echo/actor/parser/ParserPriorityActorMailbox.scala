package echo.actor.parser

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import echo.actor.ActorProtocol._

/**
  * @author Maximilian Irro
  */

class ParserPriorityActorMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
        case ActorRefDirectoryStoreActor(_) => 0
        case ActorRefCrawlerActor(_)        => 0
        case ParseUpdateEpisodeData(_,_,_)  => 1
        case ParseWebsiteData(_,_)          => 2
        case ParseNewPodcastData(_,_,_)     => 3 // this produces work, so it should be done with lower work than the processing of podcast/episode data
        case _                              => 4
    })
