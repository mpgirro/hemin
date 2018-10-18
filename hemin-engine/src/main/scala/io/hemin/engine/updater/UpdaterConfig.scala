package io.hemin.engine.updater

object UpdaterConfig {
  val dispatcherId: String = "hemin.updater.dispatcher"
}

/**
  * Configuration for [[io.hemin.engine.updater.Updater]]
  */
final case class UpdaterConfig (
  dispatcherId: String = UpdaterConfig.dispatcherId,
)
