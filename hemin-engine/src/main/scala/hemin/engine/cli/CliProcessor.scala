package hemin.engine.cli

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import hemin.engine.HeminConfig
import hemin.engine.cli.CommandLineInterpreter.CliAction
import hemin.engine.util.GuardedOperationDispatcher
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

  private val guarded: GuardedOperationDispatcher = new GuardedOperationDispatcher(bus, system, config, ec)

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
      params.search               -> runSearch
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
    def action(id: String): Future[String] = Future {
      guarded.checkPodcast(id)
      "Attempting to check podcast" // we need this result type
    }
    params.podcast.check.id.toOption.foreach(id => awaitAndPrint(action(id)))
  }

  private def proposeFeed(params: CliParams): Unit = {
    def action(urls: List[String]): Future[String] = Future {
      val out = new StringBuilder
      urls.foreach { f =>
        out ++= "proposing " + f
        guarded.proposeFeed(f)
      }
      out.mkString
    }
    params.feed.propose.url.toOption.foreach(urls => awaitAndPrint(action(urls)))
  }

  private def retrievePodcast(params: CliParams): Unit =
    params.podcast.get.id.toOption.foreach(id => awaitAndPrint(
      CliFormatter.format(guarded.getPodcast(id))
    ))

  private def retrievePodcastEpisodes(params: CliParams): Unit =
    params.podcast.episodes.get.id.toOption.foreach(id => awaitAndPrint(
      CliFormatter.format(guarded.getPodcastEpisodes(id))))

  private def retrievePodcastFeeds(params: CliParams): Unit = {
    params.podcast.feeds.get.id.toOption.foreach(id => awaitAndPrint(
      CliFormatter.format(guarded.getPodcastFeeds(id))
    ))
  }

  private def retrieveEpisode(params: CliParams): Unit = {
    params.episode.get.id.toOption.foreach(id => awaitAndPrint(
      CliFormatter.format(guarded.getEpisode(id))
    ))
  }

  private def retrieveEpisodeChapters(params: CliParams): Unit = {
    params.episode.chapters.get.id.toOption.foreach(id => awaitAndPrint(
      CliFormatter.format(guarded.getEpisodeChapters(id))
    ))
  }

  private def retrieveFeed(params: CliParams): Unit = {
    params.feed.get.id.toOption.foreach(id => awaitAndPrint(
      CliFormatter.format(guarded.getFeeds(id))
    ))
  }

  private def help(params: CliParams): Unit = params.printHelp()

  private def runSearch(params: CliParams): Unit = {
    val query      = params.search.query.getOrElse(Nil).mkString(" ")
    val pageNumber = params.search.pageNumber.toOption
    val pageSize   = params.search.pageSize.toOption
    awaitAndPrint(
      CliFormatter.format(guarded.getSearchResult(query, pageNumber, pageSize))
    )
  }

}
