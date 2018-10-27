package io.hemin.engine.util.config

trait ConfigStandardValues {

  /** Configuration name(space) */
  val namespace: String

  /** Dispatcher name */
  final lazy val dispatcher: String = s"$namespace.dispatcher"

  /** Mailbox name */
  final lazy val mailbox: String = s"$namespace.mailbox"

}
