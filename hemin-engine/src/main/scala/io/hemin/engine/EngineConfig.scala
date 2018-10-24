package io.hemin.engine

import com.typesafe.config.{Config, ConfigFactory}
import io.hemin.engine.catalog.CatalogConfig
import io.hemin.engine.crawler.CrawlerConfig
import io.hemin.engine.index.IndexConfig
import io.hemin.engine.parser.ParserConfig
import io.hemin.engine.searcher.SearcherConfig
import io.hemin.engine.updater.UpdaterConfig

import scala.concurrent.duration._

/** Configuration for [[io.hemin.engine.Engine]] */
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

  /** Load from a given a given `com.typesafe.config.Config` object.
    * To ensure a fully initialized [[io.hemin.engine.EngineConfig]],
    * the given Typesafe Config is interpolated with the results of
    * [[io.hemin.engine.EngineConfig.defaultConfig]] as the fallback
    * values for all keys that are not set in the argument config.
    */
  def load(config: Config): EngineConfig = loadFromSafeConfig(config.withFallback(defaultConfig))

  /** The default configuration of an [[io.hemin.engine.Engine]],
    * as a `com.typesafe.config.Config` object. This configuration
    * includes dispatcher and mailboxe configuration for every Akka actor.
    */
  lazy val defaultConfig: Config = ConfigFactory
    .empty()
    .withFallback(CatalogConfig.defaultConfig)
    .withFallback(CrawlerConfig.defaultConfig)
    .withFallback(IndexConfig.defaultConfig)
    .withFallback(NodeConfig.defaultConfig)
    .withFallback(ParserConfig.defaultConfig)
    .withFallback(SearcherConfig.defaultConfig)
    .withFallback(UpdaterConfig.defaultConfig)

  /** Loads the config. To be used from `main()` or equivalent. */
  def loadFromEnvironment(): EngineConfig =
    load(ConfigFactory.load(System.getProperty("config.resource", "application.conf")))

  /** All keys are expected and must be present in the config map.
    * Use [[io.hemin.engine.EngineConfig.defaultConfig()]] for the
    * fallback values to the TypeSafe Config object before calling
    * this method.
    */
  private def loadFromSafeConfig(config: Config): EngineConfig =
    EngineConfig(
      catalog = CatalogConfig(
        mongoUri       = config.getString(s"${CatalogConfig.configPath}.mongo-uri"),
        createDatabase = config.getBoolean(s"${CatalogConfig.configPath}.create-database"),
        defaultPage    = config.getInt(s"${CatalogConfig.configPath}.default-page"),
        defaultSize    = config.getInt(s"${CatalogConfig.configPath}.default-size"),
        maxPageSize    = config.getInt(s"${CatalogConfig.configPath}.max-page-size"),
      ),
      crawler = CrawlerConfig(
        workerCount      = config.getInt(s"${CrawlerConfig.configPath}.worker-count"),
        fetchWebsites    = config.getBoolean(s"${CrawlerConfig.configPath}.fetch-websites"),  // TODO rename to config file
        downloadTimeout  = config.getInt(s"${CrawlerConfig.configPath}.download-timeout"),    // TODO add to config file
        downloadMaxBytes = config.getLong(s"${CrawlerConfig.configPath}.download-max-bytes"), // = 5  * 1024 * 1024 // TODO add to config file
      ),
      index = IndexConfig(
        luceneIndexPath = config.getString(s"${IndexConfig.configPath}.lucene-index-path"), // TODO add to config file
        solrUri         = config.getString(s"${IndexConfig.configPath}.solr-uri"),
        solrQueueSize   = config.getInt(s"${IndexConfig.configPath}.solr-queue-size"),
        solrThreadCount = config.getInt(s"${IndexConfig.configPath}.solr-thread-count"),
        createIndex     = config.getBoolean(s"${IndexConfig.configPath}.create-index"),
        commitInterval  = config.getInt(s"${IndexConfig.configPath}.commit-interval").seconds,
        workerCount     = config.getInt(s"${IndexConfig.configPath}.handler-count"),
      ),
      node = NodeConfig(
        repl            = config.getBoolean(s"${NodeConfig.configPath}.repl"),
        internalTimeout = config.getInt(s"${NodeConfig.configPath}.internal-timeout").seconds,
      ),
      parser = ParserConfig(
        workerCount = config.getInt(s"${ParserConfig.configPath}.worker-count"),
      ),
      searcher = SearcherConfig(
        solrUri     = config.getString(s"${SearcherConfig.configPath}.solr-uri"),
        defaultPage = config.getInt(s"${SearcherConfig.configPath}.default-page"),
        defaultSize = config.getInt(s"${SearcherConfig.configPath}.default-size"),
      ),
      updater = UpdaterConfig(),
    )

}
