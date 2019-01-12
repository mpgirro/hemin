package io.hemin.engine.util.cli.command.podcast

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.util.cli.CliProcessor
import io.hemin.engine.util.cli.command.CliCommand

import scala.concurrent.{ExecutionContext, Future}

class PodcastGetFeedsCommand (bus: ActorRef)
                             (override implicit val executionContext: ExecutionContext,
                              override implicit val internalTimeout: Timeout)
  extends CliCommand {

  override val usageString: String = "podcast get feeds ID"

  override def eval(cmds: List[String]): Future[String] = cmds match {
    case id :: Nil => getFeedsByPodcast(id)
    case id :: _   => unsupportedCommand(cmds)
    case Nil       => usage
  }

  private def getFeedsByPodcast(id: String): Future[String] =
    CliProcessor.result(bus ? CatalogStore.GetFeedsByPodcast(id))

}
