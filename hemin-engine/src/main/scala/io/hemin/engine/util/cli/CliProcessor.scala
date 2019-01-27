package io.hemin.engine.util.cli

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import io.hemin.engine.HeminConfig
import io.hemin.engine.node.InternalOperationDispatcher
import io.hemin.engine.util.cli.CommandLineInterpreter.CliAction
import org.rogach.scallop.Subcommand

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class CliProcessor(bus: ActorRef,
                   system: ActorSystem,
                   config: HeminConfig,
                   ec: ExecutionContext) {

  private val log = Logger(getClass)

  private implicit val executionContext: ExecutionContext = ec
  private implicit val internalTimeout: Timeout = config.node.internalTimeout

  private val internal: InternalOperationDispatcher = new InternalOperationDispatcher(bus, system, config, ec)

  def eval(params: CliParams): Option[CliAction] = {
    onContains(params.subcommands,
      params.episode.get          -> retrieveEpisode,
      params.episode.chapters.get -> retrieveEpisodeChapters,
      params.feed.get             -> retrieveFeed,
      params.feed.propose         -> proposeFeed,
      params.feed                 -> runFeedCommand,
      params.help                 -> help,
      params.podcast.check        -> checkPodcast,
      params.podcast.episodes.get -> retrievePodcastEpisodes,
      params.podcast.feeds.get    -> retrievePodcastFeeds,
      params.podcast.get          -> retrievePodcast,
      params.podcast              -> runPodcastCommand,
    )
  }

  private def onContains[T](subcommands: Seq[T], actionMappings: (Subcommand, CliAction)*): Option[CliAction] = {
    actionMappings collectFirst {
      case (command, behavior) if subcommands contains command => behavior
    }
  }

  private def awaitAndPrint(action: Future[String]): Unit = {
    val future = action.map { result =>
      println(result)
    }
    val duration: Duration = config.node.internalTimeout.duration
    Await.ready(future, duration)
  }

  private def runPodcastCommand(params: CliParams): Unit =
    println("This command is not yet implemented")

  private def runFeedCommand(params: CliParams): Unit = {
    println("This command is not yet implemented")
  }

  private def checkPodcast(params: CliParams): Unit = {
    params.podcast.check.id.toOption.foreach(id => awaitAndPrint(internal.checkPodcast(id)))
  }

  private def proposeFeed(params: CliParams): Unit = {
    params.feed.propose.url.toOption.foreach(urls => awaitAndPrint(internal.proposeFeed(urls)))
  }

  private def retrievePodcast(params: CliParams): Unit =
    params.podcast.get.id.toOption.foreach(id => awaitAndPrint(
      CliFormatter.format(internal.getPodcast(id))
    ))

  private def retrievePodcastEpisodes(params: CliParams): Unit =
    params.podcast.episodes.get.id.toOption.foreach(id => awaitAndPrint(
      CliFormatter.format(internal.getPodcastEpisodes(id))))

  private def retrievePodcastFeeds(params: CliParams): Unit = {
    params.podcast.feeds.get.id.toOption.foreach(id => awaitAndPrint(
      CliFormatter.format(internal.getPodcastFeeds(id))
    ))
  }

  private def retrieveEpisode(params: CliParams): Unit = {
    params.episode.get.id.toOption.foreach(id => awaitAndPrint(
      CliFormatter.format(internal.getEpisode(id))
    ))
  }

  private def retrieveEpisodeChapters(params: CliParams): Unit = {
    params.episode.chapters.get.id.toOption.foreach(id => awaitAndPrint(
      CliFormatter.format(internal.getEpisodeChapters(id))
    ))
  }

  private def retrieveFeed(params: CliParams): Unit = {
    params.feed.get.id.toOption.foreach(id => awaitAndPrint(
      CliFormatter.format(internal.getFeeds(id))
    ))
  }

  private def help(params: CliParams): Unit = params.printHelp()

}
