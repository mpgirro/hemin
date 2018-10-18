package io.hemin.engine.index

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.index.IndexStore._

/*
object IndexStorePriorityMailbox {
  val name = "hemin.index.mailbox"
  val config: Config = load(parseString(
    s"""$name {
      mailbox-type = "${classOf[IndexStorePriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))
}
*/

/** Priority mailbox for [[io.hemin.engine.index.IndexStore]] */
class IndexStorePriorityMailbox(settings: ActorSystem.Settings, config: Config)
  extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
      case IndexSearch(_,_,_)                   => 0
      case AddDocIndexEvent(_)                  => 1
      case UpdateDocImageIndexEvent(_,_)        => 2
      case UpdateDocWebsiteDataIndexEvent(_,_)  => 2
      case UpdateDocLinkIndexEvent(_,_)         => 2
      case _                                    => 3 // other messages
    })
