package io.disposia.engine.crawler

/**
  * Configuration for [[io.disposia.engine.crawler.Crawler]]
  */
case class CrawlerConfig (
  workerCount: Int,
  fetchWebsites: Boolean,
  downloadTimeout: Int,
  downloadMaxBytes: Long
)
