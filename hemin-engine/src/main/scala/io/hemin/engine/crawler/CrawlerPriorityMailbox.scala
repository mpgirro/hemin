package io.hemin.engine.crawler

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.EngineProtocol._
import io.hemin.engine.crawler.Crawler.{DownloadContent, DownloadWithHeadCheck}

object CrawlerPriorityMailbox {
  val name = "hemin.crawler.mailbox"
  val config: Config = load(parseString(
    s"""$name {
      mailbox-type = "${classOf[CrawlerPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))
}

/** Mailbox configuration for [[io.hemin.engine.crawler.Crawler]] */
class CrawlerPriorityMailbox(settings: ActorSystem.Settings, config: Config)
  extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
        case ActorRefParserActor(_)        => 0
        case ActorRefCatalogStoreActor(_)  => 0
        case DownloadWithHeadCheck(_,_,_)  => 1
        case DownloadContent(_,_,_,_)      => 1
        case CrawlFyyd(_)                  => 1
        case _                             => 2
    })
