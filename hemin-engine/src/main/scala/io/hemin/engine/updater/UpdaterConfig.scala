package io.hemin.engine.updater

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.ConfigFallback

object UpdaterConfig extends ConfigFallback {
  override def name: String = "hemin.updater"
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
      mailbox-type = "${classOf[UpdaterPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))
}

/** Configuration for [[io.hemin.engine.updater.Updater]] */
final case class UpdaterConfig (
  // TODO add some config values
) extends ConfigFallback {
  override def name: String              = UpdaterConfig.name
  override def defaultDispatcher: Config = UpdaterConfig.defaultDispatcher
  override def defaultMailbox: Config    = UpdaterConfig.defaultMailbox
}
