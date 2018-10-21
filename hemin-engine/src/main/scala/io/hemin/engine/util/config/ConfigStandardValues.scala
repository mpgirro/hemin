package io.hemin.engine.util.config

trait ConfigStandardValues {

  /** Configuration name(space) */
  val configPath: String

  /** Dispatcher name */
  final lazy val dispatcher: String = configPath + ".dispatcher"

  /** Mailbox name */
  final lazy val mailbox: String = configPath + ".mailbox"

}
