package io.hemin.engine

import akka.util.Timeout
import com.typesafe.config.ConfigFactory.{load, parseString}
import com.typesafe.config.{Config, ConfigFactory}
import io.hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._

/** Configuration for [[io.hemin.engine.Node]],
  * which extends to [[io.hemin.engine.App]] when in standalone mode
  */
final case class NodeConfig(
  repl: Boolean,
  internalTimeout: Timeout,
) extends ConfigStandardValues {
  override val configPath: String = NodeConfig.configPath
}

object NodeConfig
  extends ConfigDefaults
    with ConfigStandardValues {

  override val configPath: String = s"${Engine.name}.${Node.name}"

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    s"$configPath.repl"             -> true,
    s"$configPath.internal-timeout" -> 5,
  ).asJava)

  /** The App does not run on an actor dispatcher */
  override protected[this] val defaultDispatcher: Config = load(parseString(
    s"""$dispatcher {
      type = Dispatcher
      executor = "thread-pool-executor"
      thread-pool-executor {
        fixed-pool-size = 2
      }
      throughput = 1
    }"""))

  /** The App does not have an actor mailbox */
  override protected[this] val defaultMailbox: Config = load(parseString(
    s"$mailbox { }"))

}
