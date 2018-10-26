package io.hemin.engine.node

import akka.util.Timeout
import com.typesafe.config.ConfigFactory.{load, parseString}
import com.typesafe.config.{Config, ConfigFactory}
import io.hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}
import io.hemin.engine.{Engine, node}

import scala.collection.JavaConverters._

/** Configuration for [[node.Node]], which extends
  * to [[io.hemin.engine.EngineApp]] when in standalone mode
  */
final case class NodeConfig(
  repl: Boolean,
  internalTimeout: Timeout,
  breakerMaxFailures: Int,
  breakerCallTimeout: Timeout,
  breakerResetTimeout: Timeout,
) extends ConfigStandardValues {
  override val configPath: String = NodeConfig.configPath
}

object NodeConfig
  extends ConfigDefaults
    with ConfigStandardValues {

  override val configPath: String = s"${Engine.name}.${Node.name}"

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    s"$configPath.repl"                  -> true,
    s"$configPath.internal-timeout"      -> 5,
    s"$configPath.breaker-max-failures"  -> 2,
    s"$configPath.breaker-call-timeout"  -> 2,
    s"$configPath.breaker-reset-timeout" -> 5,
  ).asJava)

  /** The App does not run on an actor dispatcher */
  override protected[this] val defaultDispatcher: Config = load(parseString(
    s"""$dispatcher {
      type = Dispatcher
      executor = "fork-join-executor"
      throughput = 1
      fork-join-executor {
        parallelism-min = 4
        parallelism-factor = 2.0
        parallelism-max = 10
      }
    }"""))

  /** The App does not have an actor mailbox */
  override protected[this] val defaultMailbox: Config = load(parseString(
    s"$mailbox { }"))

}
