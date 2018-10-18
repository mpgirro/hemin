package io.hemin.engine.crawler

object CrawlerConfig {
  val dispatcherId: String = "hemin.crawler.dispatcher"
}

/**
  * Configuration for [[io.hemin.engine.crawler.Crawler]]
  */
final case class CrawlerConfig (
  dispatcherId: String = CrawlerConfig.dispatcherId,
  workerCount: Int,
  fetchWebsites: Boolean,
  downloadTimeout: Int,
  downloadMaxBytes: Long
)
