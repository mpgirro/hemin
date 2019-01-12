package io.hemin.engine.util.cli

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import io.hemin.engine.EngineConfig
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.searcher.Searcher
import io.hemin.engine.util.cli.command.feed.FeedCommand
import io.hemin.engine.util.cli.command.podcast.PodcastCommand

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

  private val podcastCommand: PodcastCommand = new PodcastCommand(bus)
  private val feedCommand: FeedCommand = new FeedCommand(bus)

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
      case "help" :: _   => help()
      case "ping" :: Nil => pong
      case "echo" :: txt => echo(txt)

      /*
      case "propose" :: Nil   => usage("propose")
      case "propose" :: feeds => propose(feeds)
      */

      case "search" :: Nil          => usage("search")
      case "search" :: query :: Nil => search(query)

      /*
      case "podcast" :: "check" :: rest          => PodcastCheckCommand.eval(rest)
      case "podcast" :: "get" :: "feeds" :: rest => PodcastGetFeedsCommand.eval(rest)
      case "podcast" :: "get" :: rest            => PodcastGetCommand.eval(rest)
      */

      case "podcast" :: args => podcastCommand.eval(args)
      case "feed"    :: args => feedCommand.eval(args)

      case "episode" :: "get" :: "chapters" :: Nil       => usage("episode get chapters")
      case "episode" :: "get" :: "chapters" :: id :: Nil => getChaptersByEpisode(id)
      case "episode" :: "get" :: "chapters" :: id :: _   => usage("episode get chapters")

      case "episode" :: "get" :: Nil       => usage("episode get")
      case "episode" :: "get" :: id :: Nil => getEpisode(id)
      case "episode" :: "get" :: id :: _   => usage("episode get")

      case _ => help()
    }.getOrElse(emptyInput)

  private val usage: String =
    List(
      "USAGE:",
      podcastCommand.usageString,
      feedCommand.usageString,
    ).mkString("\n")

  /*
  private lazy val usageMap = Map(
    "propose"        -> "feed [feed [feed]]",
    "benchmark"      -> "<feed|index|search>",
    "benchmark feed" -> "feed <url>",
    "benchmark index"-> "",
    "benchmark search"-> "",
    "check podcast"  -> "[all|<id>]",
    "check feed"     -> "[all|<id>]",
    "count"          -> "[podcasts|episodes|feeds]",
    "search"         -> "query [query [query]]",
    "print database" -> "[podcasts|episodes|feeds]",
    "load feeds"     -> "[test|massive]",
    "load fyyd"      -> "[episodes <podcastId> <fyydId>]",
    "save feeds"     -> "<dest>",
    "crawl fyyd"     -> "count",
    "get podcast"    -> "<id>",
    "get episode"    -> "<id>",
    "request mean episodes" -> ""
  )
  */

  private lazy val emptyInput: Future[String] = Future.successful("Input command was empty")
  private lazy val pong: Future[String] = Future.successful("pong")

  private def echo(txt: List[String]): Future[String] = Future.successful(txt.mkString(" "))

  // TODO implement
  private def usage(cmd: String): Future[String] = Future.successful(s"USAGE not yet implemented for command : $cmd")

  private def help(): Future[String] = {
    val out = new StringBuilder
    out ++= "This is an interactive REPL to the engine. Available commands are:\n"
    out ++= usage
    Future.successful(out.mkString)
  }

  /*
  private def propose(feeds: List[String]): Future[String] = {
    val out = new StringBuilder
    feeds.foreach { f =>
      out ++= "proposing " + f
      bus ! CatalogStore.ProposeNewFeed(f)
    }
    Future.successful(out.mkString)
  }
  */

  private def search(query: String): Future[String] =
    CliFormatter.cliResult(bus ? Searcher.SearchRequest(query, Some(config.searcher.defaultPage), Some(config.searcher.defaultSize)))

  //private def getPodcast(id: String): Future[String] = result(bus ? CatalogStore.GetPodcast(id))

  private def getEpisode(id: String): Future[String] = CliFormatter.cliResult(bus ? CatalogStore.GetEpisode(id))

  //private def getFeed(id: String): Future[String] = CliFormatter.cliResult(bus ? CatalogStore.GetFeed(id))

  private def getEpisodesByPodcast(id: String): Future[String] = CliFormatter.cliResult(bus ? CatalogStore.GetEpisodesByPodcast(id))

  //private def getFeedsByPodcast(id: String): Future[String] = result(bus ? CatalogStore.GetFeedsByPodcast(id))

  private def getChaptersByEpisode(id: String): Future[String] = CliFormatter.cliResult(bus ? CatalogStore.GetChaptersByEpisode(id))

  /*
  private def checkPodcast(id: String): Future[String] = Future {
    bus ? CatalogStore.CheckPodcast(id)
    "Attempting to check podcast" // we need this result type
  }
  */

  /*
  private def result(future: Future[Any]): Future[String] = future.map {
    case CatalogStore.PodcastResult(p)            => format(p)
    case CatalogStore.EpisodeResult(e)            => format(e)
    case CatalogStore.FeedResult(f)               => format(f)
    case CatalogStore.EpisodesByPodcastResult(es) => format(es)
    case CatalogStore.FeedsByPodcastResult(fs)    => format(fs)
    case CatalogStore.ChaptersByEpisodeResult(cs) => format(cs)
    case Searcher.SearchResults(rs)               => format(rs)
    case other                                    => unhandled(other)
  }
  */

}
