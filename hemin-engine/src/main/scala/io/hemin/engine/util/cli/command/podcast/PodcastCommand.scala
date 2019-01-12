package io.hemin.engine.util.cli.command.podcast

import akka.actor.ActorRef
import akka.util.Timeout
import io.hemin.engine.util.cli.command.CliCommand

import scala.concurrent.{ExecutionContext, Future}

class PodcastCommand (bus: ActorRef)
                     (override implicit val executionContext: ExecutionContext,
                      override implicit val internalTimeout: Timeout)
  extends CliCommand {

  private val podcastCheckCommand: PodcastCheckCommand = new PodcastCheckCommand(bus)
  private val podcastGetCommand: PodcastGetCommand = new PodcastGetCommand(bus)

  override val usageString: String =
    List(
      podcastCheckCommand.usageString,
      podcastGetCommand.usageString,
    ).mkString("\n")

  override def eval(cmd: List[String]): Future[String] = cmd match {
    case "check" :: args => podcastCheckCommand.eval(args)
    case "get"   :: args => podcastGetCommand.eval(args)
    case other: Any      => unsupportedCommand(other)
  }
}
