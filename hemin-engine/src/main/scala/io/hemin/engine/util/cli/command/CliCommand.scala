package io.hemin.engine.util.cli.command

import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}

abstract class CliCommand (implicit val executionContext: ExecutionContext,
                           implicit val internalTimeout: Timeout) {
  val usageString: String
  def usage: Future[String] = Future { usageString }
  def eval(cmds: List[String]): Future[String]
  def unsupportedCommand(cmd: List[String]): Future[String] = Future {
    s"""
       Command or argument '${cmd.mkString(" ")}' not supported
       Usage:
       \t$usageString
     """.stripMargin
  }
}
