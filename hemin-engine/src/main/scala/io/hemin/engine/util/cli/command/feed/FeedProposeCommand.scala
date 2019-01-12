package io.hemin.engine.util.cli.command.feed

import akka.actor.ActorRef
import akka.util.Timeout
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.util.cli.command.CliCommand

import scala.concurrent.{ExecutionContext, Future}

class FeedProposeCommand (bus: ActorRef)
                         (override implicit val executionContext: ExecutionContext,
                          override implicit val internalTimeout: Timeout)
  extends CliCommand {

  override lazy val usageDefs: List[String] = List(
    "feed propose URL [URL [...]]",
  )

  override def eval(cmd: List[String]): Future[String] = cmd match {
    case urls: List[String] => proposeFeeds(urls)
    case _                  => usageResult
  }

  private def proposeFeeds(urls: List[String]): Future[String] = Future {
    val out = new StringBuilder
    urls.foreach { f =>
      out ++= "proposing " + f
      bus ! CatalogStore.ProposeNewFeed(f)
    }
    out.mkString
  }
}
