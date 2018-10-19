package io.hemin.engine.catalog

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.config.{ConfigStandardValues, ConfigDefaults}

import scala.collection.JavaConverters._

/** Configuration for [[io.hemin.engine.catalog.CatalogStore]] */
final case class CatalogConfig (
  mongoUri: String,
  createDatabase: Boolean,
  defaultPage: Int,
  defaultSize: Int,
  maxPageSize: Int
) extends ConfigStandardValues {
  override def configPath: String = CatalogConfig.configPath
}

object CatalogConfig
  extends ConfigDefaults
    with ConfigStandardValues {

  override def configPath: String = "hemin.catalog"

  override protected[this] def defaultValues: Config = ConfigFactory.parseMap(Map(
    configPath+".mongo-uri"       -> "mongodb://localhost:27017/hemin",
    configPath+".create-database" -> true,
    configPath+".default-page"    -> 1,
    configPath+".default-size"    -> 20,
    configPath+".max-page-size"   -> 10000,
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
      mailbox-type = "${classOf[CatalogPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))

}
