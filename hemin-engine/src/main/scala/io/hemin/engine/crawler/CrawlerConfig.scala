package io.hemin.engine.crawler

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.config.StandardConfig

import scala.collection.JavaConverters._

object CrawlerConfig extends StandardConfig {
  override def name: String = "hemin.crawler"
  override def defaultConfig: Config = ConfigFactory.parseMap(Map(
    name+".worker-count"       -> 5,
    name+".fetch-websites"     -> false, // TODO rename to config file
    name+".download-timeout"   -> 10, // TODO add to config file
    name+".download-max-bytes" -> 5242880, // = 5  * 1024 * 1024 // TODO add to config file
  ).asJava)
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
) extends StandardConfig {
  override def name: String              = CrawlerConfig.name
  override def defaultConfig: Config     = CrawlerConfig.defaultConfig
  override def defaultDispatcher: Config = CrawlerConfig.defaultDispatcher
  override def defaultMailbox: Config    = CrawlerConfig.defaultMailbox
}
