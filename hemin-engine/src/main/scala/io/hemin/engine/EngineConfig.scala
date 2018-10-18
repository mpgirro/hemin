package io.hemin.engine

import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import io.hemin.engine.catalog.CatalogConfig
import io.hemin.engine.crawler.CrawlerConfig
import io.hemin.engine.index.IndexConfig
import io.hemin.engine.parser.ParserConfig
import io.hemin.engine.searcher.SearcherConfig
import io.hemin.engine.updater.UpdaterConfig

import scala.collection.JavaConverters._
import scala.concurrent.duration._

/** Configuration for [[io.hemin.engine.Engine]] */
final case class EngineConfig(
  app: AppConfig, // Config when we start an engine stand-alone
  catalog: CatalogConfig,
  crawler: CrawlerConfig,
  index: IndexConfig,
  parser: ParserConfig,
  searcher: SearcherConfig,
  updater: UpdaterConfig,
  internalTimeout: Timeout
)

object EngineConfig {

  /** Loads the config. To be used from `main()` or equivalent. */
  def loadFromEnvironment(): EngineConfig =
    load(ConfigFactory.load(System.getProperty("config.resource", "application.conf")))

  /**
    * Load from a given a given `com.typesafe.config.Config` object.
    * To ensure a fully initialized [[io.hemin.engine.EngineConfig]],
    * the given Typesafe Config is interpolated with the results of
    * [[io.hemin.engine.EngineConfig.defaultConfig()]] as the fallback
    * values for all keys that are not set in the argument config.
    */
  def load(config: Config): EngineConfig = loadSafe(config.withFallback(defaultConfig()))

  /**
    * The default configuration of an [[io.hemin.engine.Engine]],
    * as a `com.typesafe.config.Config` object. This configuration
    * includes dispatcher and mailboxe configuration for every Akka actor.
    */
  def defaultConfig(): Config = ConfigFactory
    .parseMap(Map(
      "hemin.internal-timeout" -> 5,
    ).asJava)
    .withFallback(CatalogConfig.defaultConfig)
    .withFallback(CrawlerConfig.defaultConfig)
    .withFallback(IndexConfig.defaultConfig)
    .withFallback(ParserConfig.defaultConfig)
    .withFallback(SearcherConfig.defaultConfig)
    .withFallback(UpdaterConfig.defaultConfig)

  /**
    * All keys are expected and must be present in the config map.
    * Use [[io.hemin.engine.EngineConfig.defaultConfig()]] for the
    * fallback values to the TypeSafe Config object before calling
    * this method.
    */
  private def loadSafe(config: Config): EngineConfig =
    EngineConfig(
      app = AppConfig(),
      catalog = CatalogConfig(
        mongoUri       = config.getString("hemin.catalog.mongo-uri"),
        createDatabase = config.getBoolean("hemin.catalog.create-database"),
        defaultPage    = config.getInt("hemin.catalog.default-page"),
        defaultSize    = config.getInt("hemin.catalog.default-size"),
        maxPageSize    = config.getInt("hemin.catalog.max-page-size"),
      ),
      crawler = CrawlerConfig(
        workerCount      = config.getInt("hemin.crawler.worker-count"),
        fetchWebsites    = config.getBoolean("hemin.crawler.fetch-websites"),  // TODO rename to config file
        downloadTimeout  = config.getInt("hemin.crawler.download-timeout"),    // TODO add to config file
        downloadMaxBytes = config.getLong("hemin.crawler.download-max-bytes"), // = 5  * 1024 * 1024 // TODO add to config file
      ),
      index = IndexConfig(
        luceneIndexPath = config.getString("hemin.index.lucene-index-path"), // TODO add to config file
        solrUri         = config.getString("hemin.index.solr-uri"),
        solrQueueSize   = config.getInt("hemin.index.solr-queue-size"),
        solrThreadCount = config.getInt("hemin.index.solr-thread-count"),
        createIndex     = config.getBoolean("hemin.index.create-index"),
        commitInterval  = config.getInt("hemin.index.commit-interval").seconds,
        workerCount     = config.getInt("hemin.index.handler-count"),
      ),
      parser = ParserConfig(
        workerCount = config.getInt("hemin.parser.worker-count"),
      ),
      searcher = SearcherConfig(
        solrUri     = config.getString("hemin.searcher.solr-uri"),
        defaultPage = config.getInt("hemin.searcher.default-page"),
        defaultSize = config.getInt("hemin.searcher.default-size"),
      ),
      updater = UpdaterConfig(),
      internalTimeout = config.getInt("hemin.internal-timeout").seconds,
    )

}
