package io.hemin.engine.util.cli

import java.io.ByteArrayOutputStream

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import io.hemin.engine.HeminConfig
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.searcher.Searcher
import org.rogach.scallop.Subcommand

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

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
                   config: HeminConfig,
                   ec: ExecutionContext) {

  private val log = Logger(getClass)

  private implicit val executionContext: ExecutionContext = ec
  private implicit val internalTimeout: Timeout = config.node.internalTimeout

  private type CommandAction = (CliParams) => Unit

  private def onContains[T](subcommands: Seq[T], actionMappings: (Subcommand, CommandAction)*): Option[CommandAction] = {
    actionMappings collectFirst {
      case (command, behavior) if subcommands contains command => behavior
    }
  }
  
  private def determineAction(params: CliParams): Option[CommandAction] = {
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

  def eval(args: String): Future[String] = Option(args)
    .map(_.split(" "))
    .map(eval)
    .getOrElse(emptyInput)

  def eval(args: Array[String]): Future[String] = Option(args)
    .map(_.toList)
    .map(eval)
    .getOrElse(emptyInput)

  def eval(args: List[String]): Future[String] = {
    val params = new CliParams(args)
    determineAction(params) match {
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
  }

  private lazy val emptyInput: Future[String] = Future.successful("Input command was empty")

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
      bus ? CatalogStore.CheckPodcast(id)
      "Attempting to check podcast" // we need this result type
    }
    params.podcast.check.id.toOption.foreach(id => awaitAndPrint(action(id)))
  }

  private def proposeFeed(params: CliParams): Unit = {
    def action(urls: List[String]): Future[String] = Future {
      val out = new StringBuilder
      urls.foreach { f =>
        out ++= "proposing " + f
        bus ! CatalogStore.ProposeNewFeed(f)
      }
      out.mkString
    }
    params.feed.propose.url.toOption.foreach(urls => awaitAndPrint(action(urls)))
  }

  private def retrievePodcast(params: CliParams): Unit = {
    def action(id: String): Future[String] =
      CliFormatter.cliResult(bus ? CatalogStore.GetPodcast(id))
    params.podcast.get.id.toOption.foreach(id => awaitAndPrint(action(id)))
  }

  private def retrievePodcastEpisodes(params: CliParams): Unit = {
    def action(id: String): Future[String] =
      CliFormatter.cliResult(bus ? CatalogStore.GetEpisodesByPodcast(id))
    params.podcast.episodes.get.id.toOption.foreach(id => awaitAndPrint(action(id)))
  }

  private def retrievePodcastFeeds(params: CliParams): Unit = {
    def action(id: String): Future[String] =
      CliFormatter.cliResult(bus ? CatalogStore.GetFeedsByPodcast(id))
    params.podcast.feeds.get.id.toOption.foreach(id => awaitAndPrint(action(id)))
  }

  private def retrieveEpisode(params: CliParams): Unit = {
    def action(id: String): Future[String] =
      CliFormatter.cliResult(bus ? CatalogStore.GetEpisode(id))
    params.episode.get.id.toOption.foreach(id => awaitAndPrint(action(id)))
  }

  private def retrieveEpisodeChapters(params: CliParams): Unit = {
    def action(id: String): Future[String] =
      CliFormatter.cliResult(bus ? CatalogStore.GetChaptersByEpisode(id))
    params.episode.chapters.get.id.toOption.foreach(id => awaitAndPrint(action(id)))
  }

  private def retrieveFeed(params: CliParams): Unit = {
    def action(id: String): Future[String] =
      CliFormatter.cliResult(bus ? CatalogStore.GetFeed(id))
    params.feed.get.id.toOption.foreach(id => awaitAndPrint(action(id)))
  }

  private def help(params: CliParams): Unit =
    params.printHelp()

  private def getSearchResult(words: List[String]): Future[String] = {
    val query: String = words.mkString(" ")
    val pageNumber: Option[Int] = Some(config.searcher.defaultPage)
    val pageSize: Option[Int] = Some(config.searcher.defaultSize)
    CliFormatter.cliResult(bus ? Searcher.SearchRequest(query, pageNumber, pageSize))
  }

}
