package io.hemin.engine.index

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.Engine
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
  override val configPath: String = IndexConfig.configPath
}

object IndexConfig
  extends ConfigDefaults
    with ConfigStandardValues {

  override val configPath: String = s"${Engine.name}.${IndexStore.name}"

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    s"$configPath.lucene-index-path" -> "./data/index",
    s"$configPath.solr-uri"          -> "http://localhost:8983/solr/hemin",
    s"$configPath.solr-queue-size"   -> 20,
    s"$configPath.solr-thread-count" -> 4,
    s"$configPath.create-index"      -> false,
    s"$configPath.commit-interval"   -> 3,
    s"$configPath.handler-count"     -> 5,
  ).asJava)

  override protected[this] val defaultDispatcher: Config = load(parseString(
    s"""$dispatcher {
      type = Dispatcher
      executor = "fork-join-executor"
      throughput = 100
      fork-join-executor {
        parallelism-min = 4
        parallelism-factor = 2.0
        parallelism-max = 10
    }}"""))

  override protected[this] val defaultMailbox: Config = load(parseString(
    s"""$mailbox {
      mailbox-type = "${classOf[IndexPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))

}
