package io.hemin.engine.parser

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.Engine
import io.hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._

/** Configuration for [[io.hemin.engine.parser.Parser]] */
final case class ParserConfig (
  workerCount: Int
) extends ConfigStandardValues {
  override val namespace: String = ParserConfig.namespace
}

object ParserConfig
  extends ConfigDefaults[ParserConfig]
    with ConfigStandardValues {

  override val namespace: String = s"${Engine.name}.${Parser.name}"

  override def fromConfig(config: Config): ParserConfig =
    ParserConfig(
      workerCount = config.getInt(s"$namespace.worker-count"),
    )

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    s"$namespace.worker-count" -> 2,
  ).asJava)

  override protected[this] val defaultDispatcher: Config = load(parseString(
    s"""$dispatcher {
      type = Dispatcher
      executor = "fork-join-executor"
      throughput = 100
      fork-join-executor {
        parallelism-min = 4
        parallelism-factor = 2.0
        parallelism-max = 10
    }}"""))

  override protected[this] val defaultMailbox: Config = load(parseString(
    s"""$mailbox {
      mailbox-type = "${classOf[ParserPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))

}
