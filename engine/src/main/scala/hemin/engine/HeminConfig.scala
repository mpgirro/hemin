package hemin.engine

import com.typesafe.config.ConfigFactory.parseString
import com.typesafe.config.{Config, ConfigFactory}
import hemin.engine.catalog.CatalogConfig
import hemin.engine.crawler.CrawlerConfig
import hemin.engine.graph.GraphConfig
import hemin.engine.index.IndexConfig
import hemin.engine.node.NodeConfig
import hemin.engine.parser.ParserConfig
import hemin.engine.searcher.SearcherConfig
import hemin.engine.updater.UpdaterConfig

/** Configuration for [[HeminEngine]].
  *
  * @param catalog  Configuration for [[hemin.engine.catalog.CatalogStore]] subsystem
  * @param crawler  Configuration for [[hemin.engine.crawler.Crawler]] subsystem
  * @param graph    Configuration for [[hemin.engine.graph.GraphConfig]] subsystem
  * @param index    Configuration for [[hemin.engine.index.IndexStore]] subsystem
  * @param node     Configuration for [[hemin.engine.node.Node]] subsystem
  * @param parser   Configuration for [[hemin.engine.parser.Parser]] subsystem
  * @param searcher Configuration for [[hemin.engine.searcher.Searcher]] subsystem
  * @param updater  Configuration for [[hemin.engine.updater.Updater]] subsystem
  */
final case class HeminConfig(
  catalog:  CatalogConfig,
  crawler:  CrawlerConfig,
  graph:    GraphConfig,
  index:    IndexConfig,
  node:     NodeConfig,
  parser:   ParserConfig,
  searcher: SearcherConfig,
  updater:  UpdaterConfig,
)

object HeminConfig {

  final val namespace: String = "hemin.engine"

  /** Load from a given `com.typesafe.config.Config` object.  To ensure
    * a fully initialized [[hemin.engine.HeminConfig]], the given
    * Typesafe Config is interpolated with the results of
    * [[hemin.engine.HeminConfig.defaultConfig]] as the fallback
    * values for all keys that are not set in the argument config.
    */
  def load(config: Config): HeminConfig = Option(config)
    .map(_.withFallback(defaultConfig))
    .map(loadFromSafeConfig)
    .getOrElse(defaultEngineConfig)

  /** Loads the config file `application.conf` and initializes a
    * structure-fixed configuration instance. To be used from `main()`
    * or equivalent. The resulting [[hemin.engine.HeminConfig]]
    * will have default values for all fields that were not specified
    * in the config file. */
  def loadFromEnvironment(): HeminConfig =
    load(ConfigFactory.load(System.getProperty("config.resource", "application.conf")))

  /** The default configuration of an [[HeminEngine]], as a
    * `com.typesafe.config.Config` object. This configuration includes
    * configuration properties for the internal Akka system and
    * dispatcher and mailbox configuration for every Akka actor. */
  lazy val defaultConfig: Config = ConfigFactory.empty()
    .withFallback(defaultAkkaConfig)
    .withFallback(defaultMongoConfig)
    .withFallback(CatalogConfig.defaultConfig)
    .withFallback(CrawlerConfig.defaultConfig)
    .withFallback(GraphConfig.defaultConfig)
    .withFallback(IndexConfig.defaultConfig)
    .withFallback(NodeConfig.defaultConfig)
    .withFallback(ParserConfig.defaultConfig)
    .withFallback(SearcherConfig.defaultConfig)
    .withFallback(UpdaterConfig.defaultConfig)

  /** The default configuration of an [[HeminEngine]]
    * as a structure-fixed configuration instance. Equivalent to
    * [[hemin.engine.HeminConfig.defaultConfig]]. */
  lazy val defaultEngineConfig: HeminConfig = loadFromSafeConfig(defaultConfig)

  /** The default configuration for the interal Akka system. */
  lazy val defaultAkkaConfig: Config = ConfigFactory.load(parseString(
    s"""akka {
      loggers = ["akka.event.slf4j.Slf4jLogger"]
      loglevel = "DEBUG"

      # filter the log events using the back-end configuration (e.g. logback.xml) before they are published to the event bus.
      logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
      log-dead-letters = 0
      log-dead-letters-during-shutdown = off
    }"""))

  /** The default configuration for the asynchronous MongoDB driver. */
  lazy val defaultMongoConfig: Config = ConfigFactory.load(parseString(
    s"""mongo-async-driver {
      akka {
        loggers = ["akka.event.slf4j.Slf4jLogger"]
        loglevel = DEBUG
      }
    }"""))

  /** All keys are expected and must be present in the config map.
    * Use [[hemin.engine.HeminConfig.defaultConfig()]] for the
    * fallback values to the TypeSafe Config object before calling
    * this method. */
  private def loadFromSafeConfig(config: Config): HeminConfig =
    HeminConfig(
      catalog  = CatalogConfig.fromConfig(config),
      crawler  = CrawlerConfig.fromConfig(config),
      graph    = GraphConfig.fromConfig(config),
      index    = IndexConfig.fromConfig(config),
      node     = NodeConfig.fromConfig(config),
      parser   = ParserConfig.fromConfig(config),
      searcher = SearcherConfig.fromConfig(config),
      updater  = UpdaterConfig.fromConfig(config),
    )

}
