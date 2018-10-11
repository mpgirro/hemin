package io.hemin.engine.crawler

/**
  * Configuration for [[io.hemin.engine.crawler.Crawler]]
  */
final case class CrawlerConfig (
  workerCount: Int,
  fetchWebsites: Boolean,
  downloadTimeout: Int,
  downloadMaxBytes: Long
)
