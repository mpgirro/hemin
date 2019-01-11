package io.hemin.engine.util.cli

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import io.hemin.engine.EngineConfig
import io.hemin.engine.catalog.CatalogStore._
import io.hemin.engine.searcher.Searcher.{SearchRequest, SearchResults}
import io.hemin.engine.util.cli.CliFormatter.{format, unhandled}

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

  def eval(args: String): Future[String] = Option(args)
    .map(_.split(" "))
    .map(eval)
    .getOrElse(emptyInput)

  def eval(args: Array[String]): Future[String] = Option(args)
    .map(_.toList)
    .map(eval)
    .getOrElse(emptyInput)

  def eval(args: List[String]): Future[String] = Option(args)
    .map {
      case "help" :: _   => help()
      case "ping" :: Nil => pong
      case "echo" :: txt => echo(txt)

      case "propose" :: Nil   => usage("propose")
      case "propose" :: feeds => propose(feeds)

      case "search" :: Nil          => usage("search")
      case "search" :: query :: Nil => search(query)

      case "get" :: "podcast" :: Nil       => usage("get podcast")
      case "get" :: "podcast" :: id :: Nil => getPodcast(id)
      case "get" :: "podcast" :: id :: _   => usage("get podcast")

      case "get" :: "podcast-feeds" :: Nil       => usage("get podcast")
      case "get" :: "podcast-feeds" :: id :: Nil => getFeedsByPodcast(id)
      case "get" :: "podcast-feeds" :: id :: _   => usage("get podcast")

      case "get" :: "episode" :: Nil       => usage("get episode")
      case "get" :: "episode" :: id :: Nil => getEpisode(id)
      case "get" :: "episode" :: id :: _   => usage("get episode")

      case "get" :: "episode-chapters" :: Nil       => usage("get chapters")
      case "get" :: "episode-chapters" :: id :: Nil => getChaptersByEpisode(id)
      case "get" :: "episode-chapters" :: id :: _   => usage("get chapters")

      case _ => help()
    }.getOrElse(emptyInput)

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

  private lazy val emptyInput: Future[String] = Future.successful("Input command was empty")
  private lazy val pong: Future[String] = Future.successful("pong")

  private def echo(txt: List[String]): Future[String] = Future.successful(txt.mkString(" "))

  // TODO implement
  private def usage(cmd: String): Future[String] = Future.successful(s"USAGE not yet implemented for command : $cmd")

  private def help(): Future[String] = {
    val out = new StringBuilder
    out ++= "This is an interactive REPL to the engine. Available commands are:\n"
    for ( (k,v) <- usageMap ) {
      out ++= s"$k\t$v"
    }
    Future.successful(out.mkString)
  }

  private def propose(feeds: List[String]): Future[String] = {
    val out = new StringBuilder
    feeds.foreach { f =>
      out ++= "proposing " + f
      bus ! ProposeNewFeed(f)
    }
    Future.successful(out.mkString)
  }

  private def search(query: String): Future[String] =
    result(bus ? SearchRequest(query, Some(config.searcher.defaultPage), Some(config.searcher.defaultSize)))

  private def getPodcast(id: String): Future[String] = result(bus ? GetPodcast(id))

  private def getEpisode(id: String): Future[String] = result(bus ? GetEpisode(id))

  private def getFeed(id: String): Future[String] = result(bus ? GetFeed(id))

  private def getEpisodesByPodcast(id: String): Future[String] = result(bus ? GetEpisodesByPodcast(id))

  private def getFeedsByPodcast(id: String): Future[String] = result(bus ? GetFeedsByPodcast(id))

  private def getChaptersByEpisode(id: String): Future[String] = result(bus ? GetChaptersByEpisode(id))

  private def result(future: Future[Any]): Future[String] = future.map {
    case PodcastResult(p)            => format(p)
    case EpisodeResult(e)            => format(e)
    case FeedResult(f)               => format(f)
    case SearchResults(rs)           => format(rs)
    case EpisodesByPodcastResult(es) => format(es)
    case FeedsByPodcastResult(fs)    => format(fs)
    case ChaptersByEpisodeResult(cs) => format(cs)
    case other                       => unhandled(other)
  }

}
