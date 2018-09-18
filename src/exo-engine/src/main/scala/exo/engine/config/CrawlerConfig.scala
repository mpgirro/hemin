package exo.engine.config

/**
  * Configuration for [[exo.engine.crawler.Crawler]]
  */
case class CrawlerConfig (
    workerCount: Int,
    fetchWebsites: Boolean,
    downloadTimeout: Int,
    downloadMaxBytes: Long
)
