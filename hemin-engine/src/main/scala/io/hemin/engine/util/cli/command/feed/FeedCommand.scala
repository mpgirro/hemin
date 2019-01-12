package io.hemin.engine.util.cli.command.feed

import akka.actor.ActorRef
import akka.util.Timeout
import io.hemin.engine.util.cli.command.CliCommand

import scala.concurrent.{ExecutionContext, Future}

class FeedCommand (bus: ActorRef)
                  (override implicit val executionContext: ExecutionContext,
                   override implicit val internalTimeout: Timeout)
  extends CliCommand {

  private val feedProposeCommand: FeedProposeCommand = new FeedProposeCommand(bus)
  private val feedGetCommand: FeedGetCommand = new FeedGetCommand(bus)

  override val usageString: String =
    List(
      feedGetCommand.usageString,
      feedProposeCommand.usageString,
    ).mkString("\n")

  override def eval(cmd: List[String]): Future[String] = cmd match {
    case "get"     :: args => feedGetCommand.eval(args)
    case "propose" :: args => feedProposeCommand.eval(args)
    case other: Any        => unsupportedCommand(other)
  }
}
