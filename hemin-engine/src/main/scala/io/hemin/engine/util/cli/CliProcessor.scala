package io.hemin.engine.util.cli

import akka.actor.ActorRef
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import io.hemin.engine.EngineConfig
import io.hemin.engine.util.cli.command.episode.EpisodeCommand
import io.hemin.engine.util.cli.command.feed.FeedCommand
import io.hemin.engine.util.cli.command.podcast.PodcastCommand
import io.hemin.engine.util.cli.command.search.SearchCommand

import scala.concurrent.{ExecutionContext, Future}

/** Command language interpreter processor for interactive commands.
  * This is not a fully fledged REPL, since it does not print the
  * evaluation results.
  *
  * TODO: rewrite the eval(String): String methods to stream-based output, e.g. for proposing feeds
  *
  * @param bus
  * @param config
  * @param ec
  */
class CliProcessor(bus: ActorRef,
                   config: EngineConfig,
                   ec: ExecutionContext) {

  private val log = Logger(getClass)

  private implicit val executionContext: ExecutionContext = ec
  private implicit val internalTimeout: Timeout = config.node.internalTimeout

  private val episodeCommand: EpisodeCommand = new EpisodeCommand(bus)
  private val feedCommand: FeedCommand = new FeedCommand(bus)
  private val podcastCommand: PodcastCommand = new PodcastCommand(bus)
  private val searchCommand: SearchCommand = new SearchCommand(bus, config.searcher)

  def eval(cmd: String): Future[String] = Option(cmd)
    .map(_.split(" "))
    .map(eval)
    .getOrElse(emptyInput)

  def eval(cmd: Array[String]): Future[String] = Option(cmd)
    .map(_.toList)
    .map(eval)
    .getOrElse(emptyInput)

  def eval(cmd: List[String]): Future[String] = Option(cmd)
    .map {
      case "echo"    :: txt  => echo(txt)
      case "episode" :: args => episodeCommand.eval(args)
      case "feed"    :: args => feedCommand.eval(args)
      case "help"    :: _    => help()
      case "ping"    :: Nil  => pong
      case "podcast" :: args => podcastCommand.eval(args)
      case "search"  :: args => searchCommand.eval(args)
      case _                 => help()
    }.getOrElse(emptyInput)

  private lazy val commandDescriptions: String = List
    .concat(
      episodeCommand.usageDefs,
      feedCommand.usageDefs,
      podcastCommand.usageDefs,
      searchCommand.usageDefs,
    )
    .sorted
    .map(u => s"   $u")
    .mkString("\n")

  private lazy val usage: String = s"USAGE:\n$commandDescriptions"

  private lazy val emptyInput: Future[String] = Future.successful("Input command was empty")

  private lazy val pong: Future[String] = Future.successful("pong")

  private def echo(txt: List[String]): Future[String] = Future.successful(txt.mkString(" "))

  private def help(): Future[String] = {
    val out = new StringBuilder
    out ++= "\n"
    out ++= "This is an interactive REPL to the engine. Available commands are:\n\n"
    out ++= commandDescriptions
    Future.successful(out.mkString)
  }

}
