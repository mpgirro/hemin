package io.hemin.engine.util.cli

import com.typesafe.scalalogging.Logger

import scala.util.{Failure, Success, Try}

class CliParser {

  private val log = Logger(getClass)

  def parse(args: String): Option[CliParams] = Option(args)
    .map(_.split(" "))
    .flatMap(parse)

  def parse(args: Array[String]): Option[CliParams] = Option(args)
    .map(_.toList)
    .flatMap(parse)

  def parse(args: List[String]): Option[CliParams] = {
    Try(new CliParams(args)) match {
      case Success(params) => Some(params)
      case Failure(reason) =>
        val msg = s"Error converting command to CliParams; reason: ${reason.getMessage}"
        log.error(msg)
        println(msg)
        None
    }
  }

}
