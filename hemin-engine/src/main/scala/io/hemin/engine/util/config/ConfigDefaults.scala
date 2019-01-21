package io.hemin.engine.util.config

import com.typesafe.config.Config

trait ConfigDefaults[T] {

  /** Defaults for all values of the configuration case class */
  protected[this] val defaultValues: Config

  /** Default Akka actor dispatcher configuration */
  protected[this] val defaultDispatcher: Config

  /** Default Akka actor mailbox configuration */
  protected[this] val defaultMailbox: Config

  /** Default configuration as a `com.typesafe.config.Config` object.
    * Every key that the [[io.hemin.engine.HeminConfig]] tries to load has
    * a value defined in this defaults. Use these defaults as the fallback
    * config instance when initializing an [[io.hemin.engine.HeminEngine]] to
    * avoid errors from parsing partial configuration files.
    */
  final lazy val defaultConfig: Config = defaultValues
    .withFallback(defaultDispatcher)
    .withFallback(defaultMailbox)

  /** Instantiates this configuration from the provided configuration properties map. */
  def fromConfig(config: Config): T

}
