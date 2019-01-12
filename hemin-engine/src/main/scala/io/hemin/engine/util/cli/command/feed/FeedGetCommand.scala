package io.hemin.engine.util.cli.command.feed

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.util.cli.CliFormatter
import io.hemin.engine.util.cli.command.CliCommand

import scala.concurrent.{ExecutionContext, Future}

class FeedGetCommand (bus: ActorRef)
                     (override implicit val executionContext: ExecutionContext,
                      override implicit val internalTimeout: Timeout)
  extends CliCommand {

  override lazy val usageDefs: List[String] = List(
    "feed get ID",
  )

  override def eval(cmd: List[String]): Future[String] = cmd match {
    case id :: Nil => getFeed(id)
    case id :: _   => unsupportedCommand(cmd)
    case _         => usageResult
  }

  private def getFeed(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetFeed(id))

}
