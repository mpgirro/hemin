package io.disposia.engine

import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import io.disposia.engine.catalog.CatalogConfig
import io.disposia.engine.crawler.CrawlerConfig
import io.disposia.engine.index.IndexConfig
import io.disposia.engine.parser.ParserConfig
import io.disposia.engine.updater.UpdaterConfig

import scala.collection.JavaConverters._
import scala.concurrent.duration._

/**
  * Configuration for [[io.disposia.engine.Engine]]
  */
case class EngineConfig(
  appConfig: AppConfig,           // Config when we start an engine stand-alone
  catalogConfig: CatalogConfig,
  crawlerConfig: CrawlerConfig,
  indexConfig: IndexConfig,
  parserConfig: ParserConfig,
  updaterConfig: UpdaterConfig,
  internalTimeout: Timeout
)

object EngineConfig {
  /** Loads the configu. To be used from `main()` or equivalent. */
  def loadFromEnvironment(): EngineConfig =
    load(ConfigFactory
      .load(System.getProperty("config.resource", "application.conf"))
      .withFallback(defaultConfig()))

  /** Load from a given Typesafe Config object */
  def load(config: Config): EngineConfig =
    EngineConfig(
      appConfig = AppConfig(),
      catalogConfig = CatalogConfig(
        mongoUri       = config.getString("echo.catalog.mongo-uri"),
        createDatabase = config.getBoolean("echo.catalog.create-database"),
        defaultPage    = config.getInt("echo.catalog.default-page"),
        defaultSize    = config.getInt("echo.catalog.default-size"),
        maxPageSize    = config.getInt("echo.catalog.max-page-size")
      ),
      crawlerConfig = CrawlerConfig(
        workerCount      = config.getInt("echo.crawler.worker-count"),
        fetchWebsites    = config.getBoolean("echo.crawler.fetch-websites"),  // TODO rename to config file
        downloadTimeout  = config.getInt("echo.crawler.download-timeout"),    // TODO add to config file
        downloadMaxBytes = config.getLong("echo.crawler.download-max-bytes"), // = 5  * 1024 * 1024 // TODO add to config file
      ),
      indexConfig = IndexConfig(
        luceneIndexPath = config.getString("echo.index.lucene-index-path"), // TODO add to config file
        solrUri         = config.getString("echo.index.solr-uri"),
        solrQueueSize   = config.getInt("echo.index.solr-queue-size"),
        solrThreadCount = config.getInt("echo.index.solr-thread-count"),
        createIndex     = config.getBoolean("echo.index.create-index"),
        commitInterval  = config.getInt("echo.index.commit-interval").seconds,
        workerCount     = config.getInt("echo.index.handler-count"),
        defaultPage     = config.getInt("echo.index.default-page"),
        defaultSize     = config.getInt("echo.index.default-size"),
      ),
      parserConfig = ParserConfig(
        workerCount = config.getInt("echo.parser.worker-count")
      ),
      updaterConfig = UpdaterConfig(),
      internalTimeout = config.getInt("echo.internal-timeout").seconds
    )

  private def defaultConfig(): Config = {
    val defaults = Map(
      "echo.catalog.mongo-uri"          -> "mongodb://localhost:27017/disposia",
      "echo.catalog.create-database"    -> true,
      "echo.catalog.default-page"       -> 1,
      "echo.catalog.default-size"       -> 20,
      "echo.catalog.max-page-size"      -> 10000,
      "echo.crawler.worker-count"       -> 5,
      "echo.crawler.fetch-websites"     -> false, // TODO rename to config file
      "echo.crawler.download-timeout"   -> 10, // TODO add to config file
      "echo.crawler.download-max-bytes" -> 5242880, // = 5  * 1024 * 1024 // TODO add to config file
      "echo.index.lucene-index-path"    -> "./data/index",
      "echo.index.solr-uri"             -> "http://localhost:8983/solr/disposia",
      "echo.index.solr-queue-size"      -> 20,
      "echo.index.solr-thread-count"    -> 4,
      "echo.index.create-index"         -> false,
      "echo.index.commit-interval"      -> 3,
      "echo.index.handler-count"        -> 5,
      "echo.index.default-page"         -> 1,
      "echo.index.default-size"         -> 20,
      "echo.parser.worker-count"        -> 2,
      "echo.internal-timeout"           -> 5
    )
    ConfigFactory.parseMap(defaults.asJava)
  }
}
