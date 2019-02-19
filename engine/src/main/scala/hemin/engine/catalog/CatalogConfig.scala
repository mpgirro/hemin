package hemin.engine.catalog

import com.typesafe.config.ConfigFactory.{load, parseString}
import com.typesafe.config.{Config, ConfigFactory}
import hemin.engine.HeminConfig
import hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._

/** Configuration for [[hemin.engine.catalog.CatalogStore]] */
final case class CatalogConfig (
  mongoUri: String,
  createDatabase: Boolean,
  storeImages: Boolean,
  defaultPage: Int,
  defaultSize: Int,
  maxPageSize: Int,
) extends ConfigStandardValues {
  override val namespace: String = CatalogConfig.namespace
}

object CatalogConfig
  extends ConfigDefaults[CatalogConfig]
    with ConfigStandardValues {

  override val namespace: String = s"${HeminConfig.namespace}.${CatalogStore.name}"

  override def fromConfig(config: Config): CatalogConfig =
    CatalogConfig(
      mongoUri       = config.getString(s"$namespace.mongo-uri"),
      createDatabase = config.getBoolean(s"$namespace.create-database"),
      storeImages    = config.getBoolean(s"$namespace.store-images"),
      defaultPage    = config.getInt(s"$namespace.default-page"),
      defaultSize    = config.getInt(s"$namespace.default-size"),
      maxPageSize    = config.getInt(s"$namespace.max-page-size"),
    )

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    s"$namespace.mongo-uri"       -> "mongodb://localhost:27017/hemin",
    s"$namespace.create-database" -> true,
    s"$namespace.store-images"  -> false,
    s"$namespace.default-page"    -> 1,
    s"$namespace.default-size"    -> 20,
    s"$namespace.max-page-size"   -> 10000,
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
