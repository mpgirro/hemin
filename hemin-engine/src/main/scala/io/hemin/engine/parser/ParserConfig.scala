package io.hemin.engine.parser

object ParserConfig {
  val dispatcherId: String = "hemin.parser.dispatcher"
}

/**
  * Configuration for [[io.hemin.engine.parser.Parser]]
  */
final case class ParserConfig (
  dispatcherId: String = ParserConfig.dispatcherId,
  workerCount: Int
)
