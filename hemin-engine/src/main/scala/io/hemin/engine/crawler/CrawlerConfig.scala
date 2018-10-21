package io.hemin.engine.crawler

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._

/** Configuration for [[io.hemin.engine.crawler.Crawler]] */
final case class CrawlerConfig (
  workerCount: Int,
  fetchWebsites: Boolean,
  downloadTimeout: Int,
  downloadMaxBytes: Long
) extends ConfigStandardValues {
  override val configPath: String = CrawlerConfig.configPath
}

object CrawlerConfig
  extends ConfigDefaults
    with ConfigStandardValues {

  override val configPath: String = "hemin.crawler"

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    configPath+".worker-count"       -> 5,
    configPath+".fetch-websites"     -> false, // TODO rename to config file
    configPath+".download-timeout"   -> 10, // TODO add to config file
    configPath+".download-max-bytes" -> 5242880, // = 5  * 1024 * 1024 // TODO add to config file
  ).asJava)

  override protected[this] val defaultDispatcher: Config = load(parseString(
    s"""${this.dispatcher} {
      type = Dispatcher
      executor = "fork-join-executor"
      throughput = 100
      fork-join-executor {
        parallelism-min = 4
        parallelism-factor = 2.0
        parallelism-max = 10
    }}"""))

  override protected[this] val defaultMailbox: Config = load(parseString(
    s"""${this.mailbox} {
      mailbox-type = "${classOf[CrawlerPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))

}
