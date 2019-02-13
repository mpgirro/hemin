package hemin.engine.updater

import com.typesafe.config.ConfigFactory.{load, parseString}
import com.typesafe.config.{Config, ConfigFactory}
import hemin.engine.HeminEngine
import hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._

/** Configuration for [[hemin.engine.updater.Updater]] */
final case class UpdaterConfig (
  // TODO add some config values
) extends ConfigStandardValues {
  override val namespace: String = UpdaterConfig.namespace
}

object UpdaterConfig
  extends ConfigDefaults[UpdaterConfig]
    with ConfigStandardValues {

  override val namespace: String = s"${HeminEngine.name}.${Updater.name}"

  override def fromConfig(config: Config): UpdaterConfig = UpdaterConfig()

  override protected[this] val defaultValues: Config = ConfigFactory.empty()

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
