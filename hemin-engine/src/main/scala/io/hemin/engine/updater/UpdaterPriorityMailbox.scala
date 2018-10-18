package io.hemin.engine.updater

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.{load, parseString}

/*
object UpdaterPriorityMailbox {
  val name = "hemin.updater.mailbox"
  val config: Config = load(parseString(
    s"""$name {
      mailbox-type = "${classOf[UpdaterPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))
}
*/

/** Priority mailbox for [[io.hemin.engine.updater.Updater]] */
class UpdaterPriorityMailbox(settings: ActorSystem.Settings, config: Config)
  extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
      case _  => 0
    })
