package io.hemin.engine.catalog

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.Engine
import io.hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._

/** Configuration for [[io.hemin.engine.catalog.CatalogStore]] */
final case class CatalogConfig (
  mongoUri: String,
  createDatabase: Boolean,
  defaultPage: Int,
  defaultSize: Int,
  maxPageSize: Int
) extends ConfigStandardValues {
  override val configPath: String = CatalogConfig.configPath
}

object CatalogConfig
  extends ConfigDefaults[CatalogConfig]
    with ConfigStandardValues {

  override val configPath: String = s"${Engine.name}.${CatalogStore.name}"

  override def fromConfig(config: Config): CatalogConfig =
    CatalogConfig(
      mongoUri       = config.getString(s"$configPath.mongo-uri"),
      createDatabase = config.getBoolean(s"$configPath.create-database"),
      defaultPage    = config.getInt(s"$configPath.default-page"),
      defaultSize    = config.getInt(s"$configPath.default-size"),
      maxPageSize    = config.getInt(s"$configPath.max-page-size"),
    )

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    s"$configPath.mongo-uri"       -> "mongodb://localhost:27017/hemin",
    s"$configPath.create-database" -> true,
    s"$configPath.default-page"    -> 1,
    s"$configPath.default-size"    -> 20,
    s"$configPath.max-page-size"   -> 10000,
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
      mailbox-type = "${classOf[CatalogPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))

}
