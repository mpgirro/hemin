package hemin.engine.searcher

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config

/** Priority mailbox for [[hemin.engine.searcher.Searcher]] */
class SearcherPriorityMailbox(settings: ActorSystem.Settings, config: Config)
  extends UnboundedPriorityMailbox(
    // Create a new PriorityGenerator, lower prio means more important
    PriorityGenerator {
      case _  => 0
    })
