package io.hemin.engine.searcher

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.{load, parseString}

object SearcherConfig {
  val name: String       = "hemin.searcher"
  val dispatcher: String = name + ".dispatcher"
  val mailbox: String    = name + ".mailbox"

  /** Default actor dispatcher configuration for [[io.hemin.engine.searcher.Searcher]] */
  val defaultDispatcher: Config = load(parseString(
    s"""$dispatcher {
      type = Dispatcher
      executor = "fork-join-executor"
      throughput = 100
      fork-join-executor {
        parallelism-min = 4
        parallelism-factor = 2.0
        parallelism-max = 10
    }}"""))

  /** Default actor mailbox configuration for [[io.hemin.engine.searcher.Searcher]] */
  val defaultMailbox: Config = load(parseString(
    s"""$mailbox {
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
) {
  val name: String              = SearcherConfig.name
  val dispatcher: String        = SearcherConfig.dispatcher
  val mailbox: String           = SearcherConfig.mailbox
  val defaultDispatcher: Config = SearcherConfig.defaultDispatcher
  val defaultMailbox: Config    = SearcherConfig.defaultMailbox
}
