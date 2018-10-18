package io.hemin.engine.index

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.ConfigFallback

import scala.concurrent.duration.FiniteDuration

object IndexConfig extends ConfigFallback {
  override def name: String = "hemin.index"
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
      mailbox-type = "${classOf[IndexStorePriorityMailbox].getCanonicalName}"
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
) extends ConfigFallback {
  override def name: String              = IndexConfig.name
  override def defaultDispatcher: Config = IndexConfig.defaultDispatcher
  override def defaultMailbox: Config    = IndexConfig.defaultMailbox
}
