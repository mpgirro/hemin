package io.hemin.engine.util.cli.command.episode

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.util.cli.CliFormatter
import io.hemin.engine.util.cli.command.CliCommand

import scala.concurrent.{ExecutionContext, Future}

class EpisodeGetCommand (bus: ActorRef)
                        (override implicit val executionContext: ExecutionContext,
                         override implicit val internalTimeout: Timeout)
  extends CliCommand {

  private val episodeGetChapters: EpisodeGetChaptersCommand = new EpisodeGetChaptersCommand(bus)

  override lazy val usageDefs: List[String] = List(
    "episode get ID",
  ) ++ List.concat(
    episodeGetChapters.usageDefs,
  )

  override def eval(cmd: List[String]): Future[String] = cmd match {
    case "chapters" :: args => episodeGetChapters.eval(args)
    case id :: Nil          => getEpisode(id)
    case id :: _            => unsupportedCommand(cmd)
    case _                  => usageResult
  }

  private def getEpisode(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetEpisode(id))

}
