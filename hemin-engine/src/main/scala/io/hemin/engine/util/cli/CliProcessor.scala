package io.hemin.engine.util.cli

import java.io.ByteArrayOutputStream

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import io.hemin.engine.EngineConfig
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.searcher.Searcher
import org.rogach.scallop.Subcommand

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

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

  private type CommandAction = (CliParams) => Unit

  private def onContains[T](subcommands: Seq[T], actionMappings: (Subcommand, CommandAction)*): Option[CommandAction] = {
    actionMappings collectFirst {
      case (command, behavior) if subcommands contains command => behavior
    }
  }

  private def determineAction(params: CliParams): Option[CommandAction] = {
    onContains(params.subcommands,
      params.feed.propose         -> runFeedProposeCommand,
      params.feed                 -> runFeedCommand,
      params.help                 -> runHelpCommand,
      params.podcast.check        -> runCheckPodcastCommand,
      params.podcast.episodes.get -> runGetPodcastCommand,
      params.podcast.feeds.get    -> runGetPodcastCommand,
      params.podcast.get          -> runGetPodcastCommand,
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

  private def awaitAndPrint(future: Future[String]): Unit = {
    val duration: Duration = config.node.internalTimeout.duration
    val result = Await.ready(future, duration)
    println(result)
  }

  private def runPodcastCommand(params: CliParams): Unit =
    println("CALLED: podcast")

  private def runCheckPodcastCommand(params: CliParams): Unit =
    println(s"CALLED: podcast check ${params.podcast.check.id}")

  private def runFeedCommand(params: CliParams): Unit = {
    println("CALLED: feed")
  }

  private def runFeedProposeCommand(params: CliParams): Unit = {
    params.feed.propose.url.toOption match {
      case Some(url) => awaitAndPrint(proposeFeeds(url))
      case None     => println("No URL provided") // TODO this should not be necessary since ID is makred as required
    }
  }

  private def runGetPodcastCommand(params: CliParams): Unit = {
    //println(s"CALLED: podcast get ${params.podcast.get.id}")
    params.podcast.get.id.toOption match {
      case Some(id) => awaitAndPrint(getPodcast(id))
      case None     => println("No ID provided") // TODO this should not be necessary since ID is makred as required
    }
  }

  private def runGetPodcastEpisodesCommand(params: CliParams): Unit = {
    //println(s"CALLED: podcast get episodes ${params.podcast.get.episodes.id}")
    params.podcast.get.id.toOption match {
      case Some(id) => awaitAndPrint(getEpisodesByPodcast(id))
      case None     => println("No ID provided") // TODO this should not be necessary since ID is makred as required
    }
  }

  private def runGetPodcastFeedsCommand(params: CliParams): Unit = {
    //println(s"CALLED: podcast get feeds ${params.podcast.get.feeds.id}")
    params.podcast.get.id.toOption match {
      case Some(id) => awaitAndPrint(getFeedsByPodcast(id))
      case None     => println("No ID provided") // TODO this should not be necessary since ID is makred as required
    }
  }

  private def runHelpCommand(params: CliParams): Unit =
    params.printHelp()

  private def getFeed(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetFeed(id))

  private def getEpisode(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetEpisode(id))

  private def getChaptersByEpisode(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetChaptersByEpisode(id))

  private def proposeFeeds(urls: List[String]): Future[String] = Future {
    val out = new StringBuilder
    urls.foreach { f =>
      out ++= "proposing " + f
      bus ! CatalogStore.ProposeNewFeed(f)
    }
    out.mkString
  }

  private def checkPodcast(id: String): Future[String] = Future {
    bus ? CatalogStore.CheckPodcast(id)
    "Attempting to check podcast" // we need this result type
  }

  private def getPodcast(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetPodcast(id))

  private def getEpisodesByPodcast(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetEpisodesByPodcast(id))

  private def getFeedsByPodcast(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetFeedsByPodcast(id))

  private def getSearchResult(words: List[String]): Future[String] = {
    val query: String = words.mkString(" ")
    val pageNumber: Option[Int] = Some(config.searcher.defaultPage)
    val pageSize: Option[Int] = Some(config.searcher.defaultSize)
    CliFormatter.cliResult(bus ? Searcher.SearchRequest(query, pageNumber, pageSize))
  }

}
