package io.hemin.engine.index

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.config.StandardConfig

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration

object IndexConfig extends StandardConfig {
  override def name: String = "hemin.index"
  override def defaultConfig: Config = ConfigFactory.parseMap(Map(
    name+".lucene-index-path" -> "./data/index",
    name+".solr-uri"          -> "http://localhost:8983/solr/hemin",
    name+".solr-queue-size"   -> 20,
    name+".solr-thread-count" -> 4,
    name+".create-index"      -> false,
    name+".commit-interval"   -> 3,
    name+".handler-count"     -> 5,
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
      mailbox-type = "${classOf[IndexPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))
}

/** Configuration for [[io.hemin.engine.index.IndexStore]] */
final case class IndexConfig (
  luceneIndexPath: String,
  solrUri: String,
  solrQueueSize: Int,
  solrThreadCount: Int,
  createIndex: Boolean,
  commitInterval: FiniteDuration,
  workerCount: Int,
) extends StandardConfig {
  override def name: String              = IndexConfig.name
  override def defaultConfig: Config     = IndexConfig.defaultConfig
  override def defaultDispatcher: Config = IndexConfig.defaultDispatcher
  override def defaultMailbox: Config    = IndexConfig.defaultMailbox
}
