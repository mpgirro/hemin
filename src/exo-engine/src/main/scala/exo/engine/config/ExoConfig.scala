package exo.engine.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._

case class ExoConfig (
    appConfig: AppConfig,
    catalogConfig: CatalogConfig,
    crawlerConfig: CrawlerConfig,
    indexConfig: IndexConfig,
    parserConfig: ParserConfig,
    updaterConfig: UpdaterConfig
)

object ExoConfig {
    /** Loads your config.
      * To be used from `main()` or equivalent.
      */
    def loadFromEnvironment(): ExoConfig =
        load(ConfigFactory.load(System.getProperty(
            "config.resource", "application.conf")))

    /** Load from a given Typesafe Config object */
    def load(config: Config): ExoConfig =
        ExoConfig(
            appConfig = AppConfig(),
            catalogConfig = CatalogConfig(
                workerCount = Option(config.getInt("echo.catalog.worker-count")).getOrElse(5),
                databaseUrl = Option(config.getString("echo.catalog.database-url")).getOrElse("jdbc:h2:mem:echo1"),
                maxPageSize = Option(config.getInt("echo.catalog.max-page-size")).getOrElse(10000)
            ),
            crawlerConfig = CrawlerConfig(
                workerCount = Option(config.getInt("echo.crawler.worker-count")).getOrElse(5),
                fetchWebsites = Option(config.getBoolean("echo.crawler.fetch-websites")).getOrElse(false),      // TODO rename to config file
                downloadTimeout = Option(config.getInt("echo.crawler.download-timeout")).getOrElse(10),         // TODO add to config file
                downloadMaxBytes = Option(config.getLong("echo.crawler.download-max-bytes")).getOrElse(5242880), // = 5  * 1024 * 1024 // TODO add to config file
            ),
            indexConfig = IndexConfig(
                indexPath = Option(config.getString("echo.index.index-path")).getOrElse("/Users/max/volumes/echo/index_1"), // TODO add to config file
                createIndex = Option(config.getBoolean("echo.index.create-index")).getOrElse(false),
                commitInterval = Option(config.getInt("echo.index.commit-interval")).getOrElse(3).seconds,
                workerCount = Option(config.getInt("echo.index.handler-count")).getOrElse(5)
            ),
            parserConfig = ParserConfig(
                workerCount = Option(config.getInt("echo.parser.worker-count")).getOrElse(2)
            ),
            updaterConfig = UpdaterConfig()
        )
}
