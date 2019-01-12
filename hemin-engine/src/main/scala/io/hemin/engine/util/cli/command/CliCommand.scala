package io.hemin.engine.util.cli.command

import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}

abstract class CliCommand (implicit val executionContext: ExecutionContext,
                           implicit val internalTimeout: Timeout) {

  def eval(cmd: List[String]): Future[String]

  val usageDefs: List[String]

  lazy val usage: String = usageDefs
    .map(usage => s"\t$usage")
    .mkString("\n")

  def usageResult: Future[String] = Future { usage }

  def unsupportedCommand(cmd: List[String]): Future[String] = Future {
    s"""
       Command or argument '${cmd.mkString(" ")}' not supported
       Usage of this command:
       $usage
     """.stripMargin
  }
}
