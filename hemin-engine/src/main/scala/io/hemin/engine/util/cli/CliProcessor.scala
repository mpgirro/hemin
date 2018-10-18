package io.hemin.engine.util.cli

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import io.hemin.engine.EngineConfig
import io.hemin.engine.catalog.CatalogStore._
import io.hemin.engine.domain._
import io.hemin.engine.searcher.Searcher.{SearcherRequest, SearcherResults}
import io.hemin.engine.util.cli.CliFormatter.format

import scala.concurrent.{Await, ExecutionContext, Future}

object CliProcessor {
  val EMPTY_INPUT_MSG = "Input was NULL"
}

/**
  * command language interpreter processor for interactive commands. This is not a fully
  * fledged REPL, since it does not print the evaluation results.
  *
  * TODO: rewrite the eval(String): String methods to stream-based output, e.g. for proposing feeds
  *
  * @param bus
  * @param config
  * @param executionContext
  */
class CliProcessor(bus: ActorRef, config: EngineConfig, executionContext: ExecutionContext) {

  private val log = Logger(getClass)

  private implicit val INTERNAL_TIMEOUT: Timeout = config.internalTimeout

  def eval(args: String): String = Option(args)
    .map(_.split(" "))
    .map(eval)
    .getOrElse(CliProcessor.EMPTY_INPUT_MSG)

  def eval(args: Array[String]): String = Option(args)
    .map(_.toList)
    .map(eval)
    .getOrElse(CliProcessor.EMPTY_INPUT_MSG)

  def eval(args: List[String]): String = Option(args)
    .map {
      case "help" :: _ => help()

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
    }.getOrElse(CliProcessor.EMPTY_INPUT_MSG)

  private val usageMap = Map(
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

  // TODO implement
  private def usage(cmd: String): String = s"USAGE not yet implemented for command : $cmd"

  private def help(): String = {
    val out = new StringBuilder
    out ++= "This is an interactive REPL to the engine. Available commands are:\n"
    for ( (k,v) <- usageMap ) {
      out ++= s"$k\t$v"
    }
    out.mkString
  }

  private def propose(feeds: List[String]): String = {
    val out = new StringBuilder
    feeds.foreach { f =>
      out ++= "proposing " + f
      bus ! ProposeNewFeed(f)
    }
    out.mkString
  }

  private def search(query: String): String =
    result(bus ? SearcherRequest(query, Some(config.searcher.defaultPage), Some(config.searcher.defaultSize)))

  private def getPodcast(id: String): String = result(bus ? GetPodcast(id))

  private def getEpisode(id: String): String = result(bus ? GetEpisode(id))

  private def getFeed(id: String): String = result(bus ? GetFeed(id))

  private def getEpisodesByPodcast(id: String): String = result(bus ? GetEpisodesByPodcast(id))

  private def getFeedsByPodcast(id: String): String = result(bus ? GetFeedsByPodcast(id))

  private def getChaptersByEpisode(id: String): String = result(bus ? GetChaptersByEpisode(id))

  private def result(option: Option[Any]): String = option match {
    case Some(p: Podcast) => format(p)
    case Some(e: Episode) => format(e)
    case Some(f: Feed)    => format(f)
    case Some(c: Chapter) => format(c)
    case Some(i: Image)   => format(i)
    case Some(other)      => unhandled(other)
    case None => "No database record found"

  }

  private def result(future: Future[Any]): String = Await.result(future, INTERNAL_TIMEOUT.duration) match {
    case PodcastResult(p)            => result(p)
    case EpisodeResult(e)            => result(e)
    case FeedResult(f)               => result(f)
    case SearcherResults(rs)         => format(rs)
    case EpisodesByPodcastResult(es) => format(es)
    case FeedsByPodcastResult(fs)    => format(fs)
    case ChaptersByEpisodeResult(cs) => format(cs)

    case other => unhandled(other)
  }

  private def unhandled(unknown: Any): String = {
    val msg = s"CLI has no specific handler for type : ${unknown.getClass}"
    log.error(msg)
    msg // return to frontend (CLI/Web)
  }

}
