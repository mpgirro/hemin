package io.hemin.engine.searcher

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.ConfigFallback

object SearcherConfig extends ConfigFallback {
  override def name: String = "hemin.searcher"
  override def defaultDispatcher: Config = load(parseString(
    s"""${this.dispatcher} {
      type = Dispatcher
      executor = "fork-join-executor"
      throughput = 100
      fork-join-executor {
        parallelism-min = 4
        parallelism-factor = 2.0
        parallelism-max = 10
    }}"""))
  override def defaultMailbox: Config = load(parseString(
    s"""${this.mailbox} {
      mailbox-type = "${classOf[SearcherPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))
}

/** Configuration for [[io.hemin.engine.searcher.Searcher]] */
final case class SearcherConfig (
  solrUri: String,
  defaultPage: Int,
  defaultSize: Int,
) extends ConfigFallback {
  override def name: String              = SearcherConfig.name
  override def defaultDispatcher: Config = SearcherConfig.defaultDispatcher
  override def defaultMailbox: Config    = SearcherConfig.defaultMailbox
}
