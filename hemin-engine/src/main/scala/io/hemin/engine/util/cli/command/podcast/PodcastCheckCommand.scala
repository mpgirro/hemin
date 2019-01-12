package io.hemin.engine.util.cli.command.podcast

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.util.cli.command.CliCommand

import scala.concurrent.{ExecutionContext, Future}

class PodcastCheckCommand (bus: ActorRef)
                          (override implicit val executionContext: ExecutionContext,
                           override implicit val internalTimeout: Timeout)
  extends CliCommand {

  override val usageString: String = "podcast check ID"

  override def eval(cmd: List[String]): Future[String] = cmd match {
    case id :: Nil => checkPodcast(id)
    case id :: _   => unsupportedCommand(cmd)
    case Nil       => usage
  }

  private def checkPodcast(id: String): Future[String] = Future {
    bus ? CatalogStore.CheckPodcast(id)
    "Attempting to check podcast" // we need this result type
  }
}
