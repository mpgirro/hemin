package io.disposia.engine.config

import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._
import scala.concurrent.duration._

case class ExoConfig (
    appConfig: AppConfig,           // Config when we start an engine stand-alone
    catalogConfig: CatalogConfig,
    crawlerConfig: CrawlerConfig,
    indexConfig: IndexConfig,
    parserConfig: ParserConfig,
    updaterConfig: UpdaterConfig,
    internalTimeout: Timeout
)

object ExoConfig {
    /** Loads the configu. To be used from `main()` or equivalent. */
    def loadFromEnvironment(): ExoConfig =
        load(ConfigFactory
            .load(System.getProperty("config.resource", "application.conf"))
            .withFallback(defaultConfig()))

    /** Load from a given Typesafe Config object */
    def load(config: Config): ExoConfig =
        ExoConfig(
            appConfig = AppConfig(),
            catalogConfig = CatalogConfig(
                workerCount = config.getInt("echo.catalog.worker-count"),
                databaseUrl = config.getString("echo.catalog.database-url"),
                defaultPage = config.getInt("echo.catalog.default-page"),
                defaultSize = config.getInt("echo.catalog.default-size"),
                maxPageSize = config.getInt("echo.catalog.max-page-size")
            ),
            crawlerConfig = CrawlerConfig(
                workerCount      = config.getInt("echo.crawler.worker-count"),
                fetchWebsites    = config.getBoolean("echo.crawler.fetch-websites"),  // TODO rename to config file
                downloadTimeout  = config.getInt("echo.crawler.download-timeout"),    // TODO add to config file
                downloadMaxBytes = config.getLong("echo.crawler.download-max-bytes"), // = 5  * 1024 * 1024 // TODO add to config file
            ),
            indexConfig = IndexConfig(
                indexPath      = config.getString("echo.index.index-path"), // TODO add to config file
                createIndex    = config.getBoolean("echo.index.create-index"),
                commitInterval = config.getInt("echo.index.commit-interval").seconds,
                workerCount    = config.getInt("echo.index.handler-count")
            ),
            parserConfig = ParserConfig(
                workerCount = config.getInt("echo.parser.worker-count")
            ),
            updaterConfig = UpdaterConfig(),
            internalTimeout = config.getInt("echo.internal-timeout").seconds
        )

    private def defaultConfig(): Config = {
        val defaults = Map(
            "echo.catalog.worker-count"       -> 5,
            "echo.catalog.database-url"       -> "jdbc:h2:mem:echo1",
            "echo.catalog.default-page"       -> 1,
            "echo.catalog.default-size"       -> 20,
            "echo.catalog.max-page-size"      -> 10000,
            "echo.crawler.worker-count"       -> 5,
            "echo.crawler.fetch-websites"     -> false, // TODO rename to config file
            "echo.crawler.download-timeout"   -> 10, // TODO add to config file
            "echo.crawler.download-max-bytes" -> 5242880, // = 5  * 1024 * 1024 // TODO add to config file
            "echo.index.index-path"           -> "/Users/max/volumes/echo/index_1",
            "echo.index.create-index"         -> false,
            "echo.index.commit-interval"      -> 3,
            "echo.index.handler-count"        -> 5,
            "echo.parser.worker-count"        -> 2,
            "echo.internal-timeout"           -> 5
        )
        ConfigFactory.parseMap(defaults.asJava)
    }
}
