package io.hemin.engine.updater

object UpdaterConfig {
  val dispatcher: String = "hemin.updater.dispatcher"
}

/** Configuration for [[io.hemin.engine.updater.Updater]] */
final case class UpdaterConfig (

) {
  val dispatcher: String = UpdaterConfig.dispatcher
  val mailbox: String = UpdaterPriorityMailbox.name
}
