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

/**
  * Configuration for [[io.hemin.engine.Engine]]
  */
final case class EngineConfig(
  appConfig: AppConfig,           // Config when we start an engine stand-alone
  catalogConfig: CatalogConfig,
  crawlerConfig: CrawlerConfig,
  indexConfig: IndexConfig,
  parserConfig: ParserConfig,
  searcherConfig: SearcherConfig,
  updaterConfig: UpdaterConfig,
  internalTimeout: Timeout
)

object EngineConfig {
  /** Loads the config. To be used from `main()` or equivalent. */
  def loadFromEnvironment(): EngineConfig =
    load(ConfigFactory
      .load(System.getProperty("config.resource", "application.conf"))
      .withFallback(defaultConfig()))

  /** Load from a given Typesafe Config object */
  def load(config: Config): EngineConfig =
    EngineConfig(
      appConfig = AppConfig(),
      catalogConfig = CatalogConfig(
        mongoUri       = config.getString("hemin.catalog.mongo-uri"),
        createDatabase = config.getBoolean("hemin.catalog.create-database"),
        defaultPage    = config.getInt("hemin.catalog.default-page"),
        defaultSize    = config.getInt("hemin.catalog.default-size"),
        maxPageSize    = config.getInt("hemin.catalog.max-page-size"),
      ),
      crawlerConfig = CrawlerConfig(
        workerCount      = config.getInt("hemin.crawler.worker-count"),
        fetchWebsites    = config.getBoolean("hemin.crawler.fetch-websites"),  // TODO rename to config file
        downloadTimeout  = config.getInt("hemin.crawler.download-timeout"),    // TODO add to config file
        downloadMaxBytes = config.getLong("hemin.crawler.download-max-bytes"), // = 5  * 1024 * 1024 // TODO add to config file
      ),
      indexConfig = IndexConfig(
        luceneIndexPath = config.getString("hemin.index.lucene-index-path"), // TODO add to config file
        solrUri         = config.getString("hemin.index.solr-uri"),
        solrQueueSize   = config.getInt("hemin.index.solr-queue-size"),
        solrThreadCount = config.getInt("hemin.index.solr-thread-count"),
        createIndex     = config.getBoolean("hemin.index.create-index"),
        commitInterval  = config.getInt("hemin.index.commit-interval").seconds,
        workerCount     = config.getInt("hemin.index.handler-count"),
      ),
      parserConfig = ParserConfig(
        workerCount = config.getInt("hemin.parser.worker-count"),
      ),
      searcherConfig = SearcherConfig(
        solrUri     = config.getString("hemin.searcher.solr-uri"),
        defaultPage = config.getInt("hemin.searcher.default-page"),
        defaultSize = config.getInt("hemin.searcher.default-size"),
      ),
      updaterConfig = UpdaterConfig(),
      internalTimeout = config.getInt("hemin.internal-timeout").seconds,
    )

  private def defaultConfig(): Config = {
    val defaults = Map(
      "hemin.catalog.mongo-uri"          -> "mongodb://localhost:27017/hemin",
      "hemin.catalog.create-database"    -> true,
      "hemin.catalog.default-page"       -> 1,
      "hemin.catalog.default-size"       -> 20,
      "hemin.catalog.max-page-size"      -> 10000,
      "hemin.crawler.worker-count"       -> 5,
      "hemin.crawler.fetch-websites"     -> false, // TODO rename to config file
      "hemin.crawler.download-timeout"   -> 10, // TODO add to config file
      "hemin.crawler.download-max-bytes" -> 5242880, // = 5  * 1024 * 1024 // TODO add to config file
      "hemin.index.lucene-index-path"    -> "./data/index",
      "hemin.index.solr-uri"             -> "http://localhost:8983/solr/hemin",
      "hemin.index.solr-queue-size"      -> 20,
      "hemin.index.solr-thread-count"    -> 4,
      "hemin.index.create-index"         -> false,
      "hemin.index.commit-interval"      -> 3,
      "hemin.index.handler-count"        -> 5,
      "hemin.parser.worker-count"        -> 2,
      "hemin.searcher.solr-uri"          -> "http://localhost:8983/solr/hemin",
      "hemin.searcher.default-page"      -> 1,
      "hemin.searcher.default-size"      -> 20,
      "hemin.internal-timeout"           -> 5,
    )
    ConfigFactory.parseMap(defaults.asJava)
  }
}
