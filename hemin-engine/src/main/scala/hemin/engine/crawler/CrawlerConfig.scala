package hemin.engine.crawler

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigFactory.{load, parseString}
import hemin.engine.HeminEngine
import hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._

/** Configuration for [[hemin.engine.crawler.Crawler]] */
final case class CrawlerConfig (
  workerCount: Int,
  fetchWebsites: Boolean,
  downloadTimeout: Int,
  downloadMaxBytes: Long
) extends ConfigStandardValues {
  override val namespace: String = CrawlerConfig.namespace
}

object CrawlerConfig
  extends ConfigDefaults[CrawlerConfig]
    with ConfigStandardValues {

  override val namespace: String = s"${HeminEngine.name}.${Crawler.name}"

  override def fromConfig(config: Config): CrawlerConfig =
    CrawlerConfig(
      workerCount      = config.getInt(s"$namespace.worker-count"),
      fetchWebsites    = config.getBoolean(s"$namespace.fetch-websites"),  // TODO rename to config file
      downloadTimeout  = config.getInt(s"$namespace.download-timeout"),    // TODO add to config file
      downloadMaxBytes = config.getLong(s"$namespace.download-max-bytes"), // = 5  * 1024 * 1024 // TODO add to config file
    )

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    s"$namespace.worker-count"       -> 5,
    s"$namespace.fetch-websites"     -> false, // TODO rename to config file
    s"$namespace.download-timeout"   -> 10, // TODO add to config file
    s"$namespace.download-max-bytes" -> 5242880, // = 5  * 1024 * 1024 // TODO add to config file
  ).asJava)

  override protected[this] val defaultDispatcher: Config = load(parseString(
    s"""$dispatcher {
      type = Dispatcher
      executor = "fork-join-executor"
      throughput = 100
      fork-join-executor {
        parallelism-min = 4
        parallelism-factor = 2.0
        parallelism-max = 10
    }}"""))

  override protected[this] val defaultMailbox: Config = load(parseString(
    s"""$mailbox {
      mailbox-type = "${classOf[CrawlerPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))

}
