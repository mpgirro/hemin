package io.hemin.engine.parser

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.config.{ConfigDefaults, ConfigStandardValues}

import scala.collection.JavaConverters._

/** Configuration for [[io.hemin.engine.parser.Parser]] */
final case class ParserConfig (
  workerCount: Int
) extends ConfigStandardValues {
  override val configPath: String = ParserConfig.configPath
}

object ParserConfig
  extends ConfigDefaults
    with ConfigStandardValues {

  override val configPath: String = "hemin.parser"

  override protected[this] val defaultValues: Config = ConfigFactory.parseMap(Map(
    configPath+".worker-count" -> 2,
  ).asJava)

  override protected[this] val defaultDispatcher: Config = load(parseString(
    s"""${this.dispatcher} {
      type = Dispatcher
      executor = "fork-join-executor"
      throughput = 100
      fork-join-executor {
        parallelism-min = 4
        parallelism-factor = 2.0
        parallelism-max = 10
    }}"""))

  override protected[this] val defaultMailbox: Config = load(parseString(
    s"""${this.mailbox} {
      mailbox-type = "${classOf[ParserPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))

}
