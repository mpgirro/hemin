package io.hemin.engine.util.cli.command.episode

import akka.actor.ActorRef
import akka.util.Timeout
import io.hemin.engine.util.cli.command.CliCommand

import scala.concurrent.{ExecutionContext, Future}

class EpisodeCommand (bus: ActorRef)
                     (override implicit val executionContext: ExecutionContext,
                      override implicit val internalTimeout: Timeout)
  extends CliCommand {

  private val episodeGetCommand: EpisodeGetCommand = new EpisodeGetCommand(bus)

  override lazy val usageDefs: List[String] = List.concat(
    episodeGetCommand.usageDefs,
  )

  override def eval(cmd: List[String]): Future[String] = cmd match {
    case "get"     :: args => episodeGetCommand.eval(args)
    case other: Any        => unsupportedCommand(other)
  }

}
