package io.hemin.engine.crawler

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.ConfigFallback

object CrawlerConfig extends ConfigFallback {
  override def name: String = "hemin.crawler"
  override def defaultDispatcher: Config = load(parseString(
    s"""${this.dispatcher} {
      type = Dispatcher
      executor = "fork-join-executor"
      throughput = 100
      fork-join-executor {
        parallelism-min = 4
        parallelism-factor = 2.0
        parallelism-max = 10
    }}"""))
  override def defaultMailbox: Config = load(parseString(
    s"""${this.mailbox} {
      mailbox-type = "${classOf[CrawlerPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))
}

/** Configuration for [[io.hemin.engine.crawler.Crawler]] */
final case class CrawlerConfig (
  workerCount: Int,
  fetchWebsites: Boolean,
  downloadTimeout: Int,
  downloadMaxBytes: Long
) extends ConfigFallback {
  override def name: String              = CrawlerConfig.name
  override def defaultDispatcher: Config = CrawlerConfig.defaultDispatcher
  override def defaultMailbox: Config    = CrawlerConfig.defaultMailbox
}
