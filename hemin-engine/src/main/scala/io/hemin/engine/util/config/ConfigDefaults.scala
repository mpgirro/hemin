package io.hemin.engine.util.config

import com.typesafe.config.Config

trait ConfigDefaults {

  /** Defaults for all values of the configuration case class */
  protected[this] def defaultValues: Config

  /** Default actor dispatcher configuration */
  protected[this] def defaultDispatcher: Config

  /** Default actor mailbox configuration */
  protected[this] def defaultMailbox: Config

  /**
    * Default configuration as a `com.typesafe.config.Config` object.
    * Every key that the [[io.hemin.engine.EngineConfig]] tries to load has
    * a value defined in this defaults. Use these defaults as the fallback
    * config instance when initializing an [[io.hemin.engine.Engine]] to
    * avoid errors from parsing partial configuration files.
    */
  final def defaultConfig: Config = defaultValues
    .withFallback(defaultDispatcher)
    .withFallback(defaultMailbox)

}
