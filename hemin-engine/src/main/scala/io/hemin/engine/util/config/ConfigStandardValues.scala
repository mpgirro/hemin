package io.hemin.engine.util.config

trait ConfigStandardValues {

  /** Configuration name(space) */
  def name: String

  /** Dispatcher name */
  final def dispatcher: String = name + ".dispatcher"

  /** Mailbox name */
  final def mailbox: String = name + ".mailbox"

}
