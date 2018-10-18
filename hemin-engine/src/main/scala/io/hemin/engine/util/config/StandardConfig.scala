package io.hemin.engine.util.config

import com.typesafe.config.Config

trait StandardConfig {

  /** Configuration name(space) */
  def name: String

  /** Dispatcher name */
  final def dispatcher: String = name + ".dispatcher"

  /** Mailbox name */
  final def mailbox: String    = name + ".mailbox"

  /** Default configuration */
  def defaultConfig: Config

  /** Default actor dispatcher configuration */
  def defaultDispatcher: Config

  /** Default actor mailbox configuration */
  def defaultMailbox: Config

}
