package io.hemin.engine.util.cli.command.podcast

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.util.cli.CliFormatter
import io.hemin.engine.util.cli.command.CliCommand

import scala.concurrent.{ExecutionContext, Future}

class PodcastGetEpisodesCommand (bus: ActorRef)
                                (override implicit val executionContext: ExecutionContext,
                                 override implicit val internalTimeout: Timeout)
  extends CliCommand {

  override lazy val usageDefs: List[String] = List(
    "podcast get episodes ID",
  )

  override def eval(cmd: List[String]): Future[String] = cmd match {
    case id :: Nil => getEpisodesByPodcast(id)
    case id :: _   => unsupportedCommand(cmd)
    case Nil       => usageResult
  }

  private def getEpisodesByPodcast(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetEpisodesByPodcast(id))


}
