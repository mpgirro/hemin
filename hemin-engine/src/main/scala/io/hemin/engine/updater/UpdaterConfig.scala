package io.hemin.engine.updater

import com.typesafe.config.ConfigFactory.{load, parseString}
import com.typesafe.config.{Config, ConfigFactory}
import io.hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._

/** Configuration for [[io.hemin.engine.updater.Updater]] */
final case class UpdaterConfig (
  // TODO add some config values
) extends ConfigStandardValues {
  override val configPath: String = UpdaterConfig.configPath
}

object UpdaterConfig
  extends ConfigDefaults
    with ConfigStandardValues {

  override val configPath: String = "hemin.updater"

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    s"$configPath.solr-uri" -> "http://localhost:8983/solr/hemin",
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
      mailbox-type = "${classOf[UpdaterPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))

}
