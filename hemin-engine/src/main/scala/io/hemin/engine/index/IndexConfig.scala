package io.hemin.engine.index

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._
import scala.concurrent.duration.FiniteDuration

/** Configuration for [[io.hemin.engine.index.IndexStore]] */
final case class IndexConfig (
  luceneIndexPath: String,
  solrUri: String,
  solrQueueSize: Int,
  solrThreadCount: Int,
  createIndex: Boolean,
  commitInterval: FiniteDuration,
  workerCount: Int,
) extends ConfigStandardValues {
  override def configPath: String = IndexConfig.configPath
}

object IndexConfig
  extends ConfigDefaults
    with ConfigStandardValues {

  override def configPath: String = "hemin.index"
  override protected[this] def defaultValues: Config = ConfigFactory.parseMap(Map(
    configPath+".lucene-index-path" -> "./data/index",
    configPath+".solr-uri"          -> "http://localhost:8983/solr/hemin",
    configPath+".solr-queue-size"   -> 20,
    configPath+".solr-thread-count" -> 4,
    configPath+".create-index"      -> false,
    configPath+".commit-interval"   -> 3,
    configPath+".handler-count"     -> 5,
  ).asJava)
  override protected[this] def defaultDispatcher: Config = load(parseString(
    s"""${this.dispatcher} {
      type = Dispatcher
      executor = "fork-join-executor"
      throughput = 100
      fork-join-executor {
        parallelism-min = 4
        parallelism-factor = 2.0
        parallelism-max = 10
    }}"""))
  override protected[this] def defaultMailbox: Config = load(parseString(
    s"""${this.mailbox} {
      mailbox-type = "${classOf[IndexPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))
}
