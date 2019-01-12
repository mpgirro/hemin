package io.hemin.engine.util.cli.command.episode

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.util.cli.CliFormatter
import io.hemin.engine.util.cli.command.CliCommand

import scala.concurrent.{ExecutionContext, Future}

class EpisodeGetChaptersCommand (bus: ActorRef)
                                (override implicit val executionContext: ExecutionContext,
                                 override implicit val internalTimeout: Timeout)
  extends CliCommand {

  override lazy val usageDefs: List[String] = List(
    "episode get chapters ID",
  )

  override def eval(cmd: List[String]): Future[String] = cmd match {
    case id :: Nil => getChaptersByEpisode(id)
    case id :: _   => unsupportedCommand(cmd)
    case Nil       => usageResult
  }

  private def getChaptersByEpisode(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetChaptersByEpisode(id))

}
