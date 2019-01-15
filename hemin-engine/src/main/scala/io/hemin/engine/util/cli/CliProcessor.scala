package io.hemin.engine.util.cli

import java.io.ByteArrayOutputStream

import akka.actor.ActorRef
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import io.hemin.engine.EngineConfig
import io.hemin.engine.util.cli.command.episode.EpisodeCommand
import io.hemin.engine.util.cli.command.feed.FeedCommand
import io.hemin.engine.util.cli.command.podcast.PodcastCommand
import io.hemin.engine.util.cli.command.search.SearchCommand
import io.hemin.engine.util.cli.new2.CliParams
import org.rogach.scallop.Subcommand

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
    .map { args =>

      def runPodcastCommand(params: CliParams): Unit =
        println("CALLED: podcast")

      def runCheckPodcastCommand(params: CliParams): Unit =
        println(s"CALLED: podcast check ${params.podcast.check.id}")

      def runGetPodcastCommand(params: CliParams): Unit =
        println(s"CALLED: podcast get ${params.podcast.get.id}")

      def runHelpCommand(params: CliParams): Unit =
        params.printHelp()

      val params = new CliParams(args)

      type CommandAction = (CliParams) => Unit

      def onContains[T](subcommands: Seq[T], actionMappings: (Subcommand, CommandAction)*): Option[CommandAction] = {
        actionMappings collectFirst {
          case (command, behavior) if subcommands contains command => behavior
        }
      }

      onContains(params.subcommands,
        params.help          -> runHelpCommand,
        params.podcast.check -> runCheckPodcastCommand,
        params.podcast.get   -> runGetPodcastCommand,
        params.podcast       -> runPodcastCommand,
      ) match {
        case Some(action) => Future {
          val out = new ByteArrayOutputStream
          Console.withOut(out) {
            Console.withErr(out) {
              //println(s"CLI parser results : ${params.summary}")
              action(params)
            }
          }
          out.toString
        }
        case None => Future.successful("Unknown command")
      }

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
