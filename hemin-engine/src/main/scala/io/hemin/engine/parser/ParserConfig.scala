package io.hemin.engine.parser

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.{load, parseString}
import io.hemin.engine.util.ConfigFallback

object ParserConfig extends ConfigFallback {
  override def name: String = "hemin.parser"
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
) extends ConfigFallback {
  override def name: String              = ParserConfig.name
  override def defaultDispatcher: Config = ParserConfig.defaultDispatcher
  override def defaultMailbox: Config    = ParserConfig.defaultMailbox
}
