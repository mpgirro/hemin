package io.hemin.engine.util.config

trait ConfigStandardValues {

  /** Configuration name(space) */
  val namespace: String

  /** Akka actor dispatcher name */
  final lazy val dispatcher: String = s"$namespace.dispatcher"

  /** Akka actor mailbox name */
  final lazy val mailbox: String = s"$namespace.mailbox"

}
