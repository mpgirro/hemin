package io.hemin.engine.searcher

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.config.StandardConfig

import scala.collection.JavaConverters._

/** Configuration for [[io.hemin.engine.searcher.Searcher]] */
final case class SearcherConfig (
  solrUri: String,
  defaultPage: Int,
  defaultSize: Int,
) extends StandardConfig {
  override def name: String              = SearcherConfig.name
  override def defaultConfig: Config     = SearcherConfig.defaultConfig
  override def defaultDispatcher: Config = SearcherConfig.defaultDispatcher
  override def defaultMailbox: Config    = SearcherConfig.defaultMailbox
}

object SearcherConfig extends StandardConfig {
  override def name: String = "hemin.searcher"
  override def defaultConfig: Config = ConfigFactory.parseMap(Map(
    name+".solr-uri"     -> "http://localhost:8983/solr/hemin",
    name+".default-page" -> 1,
    name+".default-size" -> 20,
  ).asJava)
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
