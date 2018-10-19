package io.hemin.engine.util.config

trait ConfigStandardValues {

  /** Configuration name(space) */
  def configPath: String

  /** Dispatcher name */
  final def dispatcher: String = configPath + ".dispatcher"

  /** Mailbox name */
  final def mailbox: String = configPath + ".mailbox"

}
