package io.hemin.engine.crawler

object CrawlerConfig {
  val dispatcher: String = "hemin.crawler.dispatcher"
}

/** Configuration for [[io.hemin.engine.crawler.Crawler]] */
final case class CrawlerConfig (
  workerCount: Int,
  fetchWebsites: Boolean,
  downloadTimeout: Int,
  downloadMaxBytes: Long
) {
  val dispatcher: String = CrawlerConfig.dispatcher
  val mailbox: String = CrawlerPriorityMailbox.name
}
