package io.hemin.engine.cli

import com.typesafe.config.ConfigFactory.{load, parseString}
import com.typesafe.config.{Config, ConfigFactory}
import io.hemin.engine.HeminConfig
import io.hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

/** Configuration for [[io.hemin.engine.cli.CommandLineInterpreter]] */
final case class CliConfig () extends ConfigStandardValues {
  override val namespace: String = CliConfig.namespace
}

object CliConfig
  extends ConfigDefaults[CliConfig]
    with ConfigStandardValues {

  override val namespace: String = s"${HeminConfig.namespace}.${CommandLineInterpreter.name}"

  override def fromConfig(config: Config): CliConfig =
    CliConfig()

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
      mailbox-type = "${classOf[CliMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))


}
