package io.hemin.engine.node

import akka.util.Timeout
import com.typesafe.config.ConfigFactory.{load, parseString}
import com.typesafe.config.{Config, ConfigFactory}
import io.hemin.engine.HeminConfig
import io.hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._
import scala.concurrent.duration._

/** Configuration for [[io.hemin.engine.node.Node]], which extends
  * to [[io.hemin.engine.HeminApp]] when in standalone mode
  */
final case class NodeConfig(
  repl: Boolean,
  internalTimeout: Timeout,
  breakerMaxFailures: Int,
  breakerCallTimeout: Timeout,
  breakerResetTimeout: Timeout,
) extends ConfigStandardValues {
  override val namespace: String = NodeConfig.namespace
}

object NodeConfig
  extends ConfigDefaults[NodeConfig]
    with ConfigStandardValues {

  override val namespace: String = s"${HeminConfig.namespace}.${Node.name}"

  override def fromConfig(config: Config): NodeConfig =
    NodeConfig(
      repl                = config.getBoolean(s"$namespace.repl"),
      internalTimeout     = config.getInt(s"$namespace.internal-timeout").seconds,
      breakerMaxFailures  = config.getInt(s"$namespace.breaker-max-failures"),
      breakerCallTimeout  = config.getInt(s"$namespace.breaker-call-timeout").seconds,
      breakerResetTimeout = config.getInt(s"$namespace.breaker-reset-timeout").seconds,
    )

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    s"$namespace.repl"                  -> true,
    s"$namespace.internal-timeout"      -> 5,
    s"$namespace.breaker-max-failures"  -> 2,
    s"$namespace.breaker-call-timeout"  -> 2,
    s"$namespace.breaker-reset-timeout" -> 5,
  ).asJava)

  /** The App does not run on an actor dispatcher */
  override protected[this] val defaultDispatcher: Config = load(parseString(
    s"""$dispatcher {
      type = Dispatcher
      executor = "fork-join-executor"
      throughput = 10
      fork-join-executor {
        parallelism-min = 4
        parallelism-factor = 2.0
        parallelism-max = 10
    }}"""))

  /** The App does not have an actor mailbox */
  override protected[this] val defaultMailbox: Config = load(parseString(
    s"""$mailbox {
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))

}
