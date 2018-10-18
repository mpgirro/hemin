package io.hemin.engine.util.config

import com.typesafe.config.Config

trait StandardConfig {

  /** Configuration name(space) */
  def name: String

  /** Dispatcher name */
  final def dispatcher: String = name + ".dispatcher"

  /** Mailbox name */
  final def mailbox: String = name + ".mailbox"

  /**
    * Default configuration as a `com.typesafe.config.Config` instance.
    * Every key that the [[io.hemin.engine.EngineConfig]] tries to load has
    * a value defined in this defaults. Use these defaults as the fallback
    * config instance when initializing an [[io.hemin.engine.Engine]] to
    * avoid errors due to partial configuration files.
    */
  def defaultConfig: Config

  /** Default actor dispatcher configuration */
  def defaultDispatcher: Config

  /** Default actor mailbox configuration */
  def defaultMailbox: Config

}
