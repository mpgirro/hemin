package io.hemin.engine.util.cli.command.podcast

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.util.cli.CliProcessor
import io.hemin.engine.util.cli.command.CliCommand

import scala.concurrent.{ExecutionContext, Future}

class PodcastGetCommand (bus: ActorRef)
                        (override implicit val executionContext: ExecutionContext,
                         override implicit val internalTimeout: Timeout)
  extends CliCommand {

  private val podcastGetFeedsCommand: PodcastGetFeedsCommand = new PodcastGetFeedsCommand(bus)

  override val usageString: String =
    List(
      podcastGetFeedsCommand.usageString,
      "podcast get ID"
    ).mkString("\n")

  override def eval(cmds: List[String]): Future[String] = cmds match {
    case "feeds" :: args => podcastGetFeedsCommand.eval(args)
    case id :: Nil       => getPodcast(id)
    case id :: _         => unsupportedCommand(cmds)
    case Nil             => usage
  }

  private def getPodcast(id: String): Future[String] = CliProcessor.result(bus ? CatalogStore.GetPodcast(id))

}
