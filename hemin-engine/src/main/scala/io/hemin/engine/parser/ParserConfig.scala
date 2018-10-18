package io.hemin.engine.parser

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.config.StandardConfig

import scala.collection.JavaConverters._

object ParserConfig extends StandardConfig {
  override def name: String = "hemin.parser"
  override def defaultConfig: Config = ConfigFactory.parseMap(Map(
    name+".worker-count" -> 2,
  ).asJava)
  override def defaultDispatcher: Config = load(parseString(
    s"""${this.dispatcher} {
      type = Dispatcher
      executor = "fork-join-executor"
      throughput = 100
      fork-join-executor {
        parallelism-min = 4
        parallelism-factor = 2.0
        parallelism-max = 10
    }}"""))
  override def defaultMailbox: Config = load(parseString(
    s"""${this.mailbox} {
      mailbox-type = "${classOf[ParserPriorityMailbox].getCanonicalName}"
      mailbox-capacity = 100
      mailbox-push-timeout-time = 1ms
    }"""))
}

/** Configuration for [[io.hemin.engine.parser.Parser]] */
final case class ParserConfig (
  workerCount: Int
) extends StandardConfig {
  override def name: String              = ParserConfig.name
  override def defaultConfig: Config     = ParserConfig.defaultConfig
  override def defaultDispatcher: Config = ParserConfig.defaultDispatcher
  override def defaultMailbox: Config    = ParserConfig.defaultMailbox
}
