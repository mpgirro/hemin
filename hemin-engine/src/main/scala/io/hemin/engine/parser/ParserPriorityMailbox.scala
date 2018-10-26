package io.hemin.engine.parser

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import io.hemin.engine.Node.{ActorRefCatalogStoreActor, ActorRefCrawlerActor}
import io.hemin.engine.parser.Parser.{ParseNewPodcastData, ParseUpdateEpisodeData, ParseWebsiteData}

/** Priority mailbox for [[io.hemin.engine.parser.Parser]] */
class ParserPriorityMailbox(settings: ActorSystem.Settings, config: Config)
  extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
        case ActorRefCatalogStoreActor(_)  => 0
        case ActorRefCrawlerActor(_)       => 0
        case ParseUpdateEpisodeData(_,_,_) => 1
        case ParseWebsiteData(_,_)         => 2
        case ParseNewPodcastData(_,_,_)    => 3 // this produces work, so it should be done with lower work than the processing of podcast/episode data
        case _                             => 4
    })
