package io.hemin.engine

import com.typesafe.config.{Config, ConfigFactory}
import io.hemin.engine.catalog.CatalogConfig
import io.hemin.engine.crawler.CrawlerConfig
import io.hemin.engine.index.IndexConfig
import io.hemin.engine.node.NodeConfig
import io.hemin.engine.parser.ParserConfig
import io.hemin.engine.searcher.SearcherConfig
import io.hemin.engine.updater.UpdaterConfig

import scala.concurrent.duration._

/** Configuration for [[io.hemin.engine.Engine]]
  *
  * @param catalog Configuration for [[io.hemin.engine.catalog.CatalogStore]] subsystem
  * @param crawler Configuration for [[io.hemin.engine.crawler.Crawler]] subsystem
  * @param index Configuration for [[io.hemin.engine.index.IndexStore]] subsystem
  * @param node Configuration for [[io.hemin.engine.node.Node]] subsystem manager
  * @param parser Configuration for [[io.hemin.engine.parser.Parser]] subsystem
  * @param searcher Configuration for [[io.hemin.engine.searcher.Searcher]] subsystem
  * @param updater Configuration for [[io.hemin.engine.updater.Updater]] subsystem
  */
final case class EngineConfig(
  catalog: CatalogConfig,
  crawler: CrawlerConfig,
  index: IndexConfig,
  node: NodeConfig,
  parser: ParserConfig,
  searcher: SearcherConfig,
  updater: UpdaterConfig,
)

object EngineConfig {

  /** Load from a given `com.typesafe.config.Config` object.  To ensure
    * a fully initialized [[io.hemin.engine.EngineConfig]], the given
    * Typesafe Config is interpolated with the results of
    * [[io.hemin.engine.EngineConfig.defaultConfig]] as the fallback
    * values for all keys that are not set in the argument config.
    */
  def load(config: Config): EngineConfig = loadFromSafeConfig(config.withFallback(defaultConfig))

  /** Loads the config file `application.conf` and initializes a
    * structure-fixed configuration instance. To be used from `main()`
    * or equivalent. The resulting [[io.hemin.engine.EngineConfig]]
    * will have default values for all fields that were not specified
    * in the config file. */
  def loadFromEnvironment(): EngineConfig =
    load(ConfigFactory.load(System.getProperty("config.resource", "application.conf")))

  /** The default configuration of an [[io.hemin.engine.Engine]], as a
    * `com.typesafe.config.Config` object. This configuration includes
    * dispatcher and mailbox configuration for every Akka actor. */
  lazy val defaultConfig: Config = ConfigFactory
    .empty()
    .withFallback(CatalogConfig.defaultConfig)
    .withFallback(CrawlerConfig.defaultConfig)
    .withFallback(IndexConfig.defaultConfig)
    .withFallback(NodeConfig.defaultConfig)
    .withFallback(ParserConfig.defaultConfig)
    .withFallback(SearcherConfig.defaultConfig)
    .withFallback(UpdaterConfig.defaultConfig)

  /** The default configuration of an [[io.hemin.engine.Engine]]
    * as a structure-fixed configuration instance. Equivalent to
    * [[io.hemin.engine.EngineConfig.defaultConfig]] */
  lazy val defaultEngineConfig: EngineConfig = loadFromSafeConfig(defaultConfig)

  /** All keys are expected and must be present in the config map.
    * Use [[io.hemin.engine.EngineConfig.defaultConfig()]] for the
    * fallback values to the TypeSafe Config object before calling
    * this method. */
  private def loadFromSafeConfig(config: Config): EngineConfig =
    EngineConfig(
      catalog  = CatalogConfig.fromConfig(config),
      crawler  = CrawlerConfig.fromConfig(config),
      index    = IndexConfig.fromConfig(config),
      node     = NodeConfig.fromConfig(config),
      parser   = ParserConfig.fromConfig(config),
      searcher = SearcherConfig.fromConfig(config),
      updater  = UpdaterConfig.fromConfig(config),
    )

}
