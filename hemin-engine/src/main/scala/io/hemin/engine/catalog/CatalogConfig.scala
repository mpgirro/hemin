package io.hemin.engine.catalog

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.ConfigFallback

object CatalogConfig extends ConfigFallback {
  override def name: String = "hemin.catalog"
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
      mailbox-type = "${classOf[CatalogPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))
}

/** Configuration for [[io.hemin.engine.catalog.CatalogStore]] */
final case class CatalogConfig (
  mongoUri: String,
  createDatabase: Boolean,
  defaultPage: Int,
  defaultSize: Int,
  maxPageSize: Int
) extends ConfigFallback {
  override def name: String              = CatalogConfig.name
  override def defaultDispatcher: Config = CatalogConfig.defaultDispatcher
  override def defaultMailbox: Config    = CatalogConfig.defaultMailbox
}
