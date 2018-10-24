package io.hemin.engine

import com.typesafe.config.ConfigFactory.{load, parseString}
import com.typesafe.config.{Config, ConfigFactory}
import io.hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._

/** Configuration for [[io.hemin.engine.App]] */
final case class AppConfig (
  repl: Boolean,
) extends ConfigStandardValues {
  override val configPath: String = AppConfig.configPath
}

object AppConfig
  extends ConfigDefaults
    with ConfigStandardValues {

  override val configPath: String = "hemin.app"

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    s"$configPath.repl" -> true
  ).asJava)

  // TODO run the NodeMaster on a dedicated dispatcher
  /** The App does not run on an actor dispatcher */
  override protected[this] val defaultDispatcher: Config = load(parseString(
    s"$dispatcher { }"))

  // TODO run the NodeMaster on a dedicated mailbox
  /** The App does not have an actor mailbox */
  override protected[this] val defaultMailbox: Config = load(parseString(
    s"$mailbox { }"))

}
