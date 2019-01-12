package io.hemin.engine.util.cli.command.podcast

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.util.cli.CliFormatter
import io.hemin.engine.util.cli.command.CliCommand

import scala.concurrent.{ExecutionContext, Future}

class PodcastGetCommand (bus: ActorRef)
                        (override implicit val executionContext: ExecutionContext,
                         override implicit val internalTimeout: Timeout)
  extends CliCommand {

  private val podcastGetEpisodesCommand: PodcastGetEpisodesCommand = new PodcastGetEpisodesCommand(bus)
  private val podcastGetFeedsCommand: PodcastGetFeedsCommand = new PodcastGetFeedsCommand(bus)

  override lazy val usageDefs: List[String] = List(
    "podcast get ID"
  ) ++ List.concat(
    podcastGetEpisodesCommand.usageDefs,
    podcastGetFeedsCommand.usageDefs,
  )

  override def eval(cmd: List[String]): Future[String] = cmd match {
    case "episodes" :: args => podcastGetEpisodesCommand.eval(args)
    case "feeds"    :: args => podcastGetFeedsCommand.eval(args)
    case id         :: Nil  => getPodcast(id)
    case id         :: _    => unsupportedCommand(cmd)
    case Nil                => usageResult
  }

  private def getPodcast(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetPodcast(id))

}
