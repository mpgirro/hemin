package io.hemin.engine.cli

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config

/** Priority mailbox for [[io.hemin.engine.cli.CommandLineInterpreter]] */
class CliMailbox(settings: ActorSystem.Settings, config: Config)
  extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
      case _  => 0
    })
