package io.hemin.engine.parser

object ParserConfig {
  val dispatcher: String = "hemin.parser.dispatcher"
}

/** Configuration for [[io.hemin.engine.parser.Parser]] */
final case class ParserConfig (
  workerCount: Int
) {
  val dispatcher: String = ParserConfig.dispatcher
  val mailbox: String = ParserPriorityMailbox.name
}
