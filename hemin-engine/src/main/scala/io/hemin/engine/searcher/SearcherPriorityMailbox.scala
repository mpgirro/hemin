package io.hemin.engine.searcher

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.{load, parseString}

/*
object SearcherPriorityMailbox {
  val name = "hemin.searcher.mailbox"
  val config: Config = load(parseString(
    s"""$name {
      mailbox-type = "${classOf[SearcherPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))
}
*/

/** Mailbox for [[io.hemin.engine.searcher.Searcher]] */
class SearcherPriorityMailbox(settings: ActorSystem.Settings, config: Config)
  extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
      case _  => 0
    })
