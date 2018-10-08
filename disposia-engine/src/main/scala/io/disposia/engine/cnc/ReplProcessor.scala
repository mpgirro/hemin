package io.disposia.engine.cnc

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import io.disposia.engine.EngineConfig
import io.disposia.engine.catalog.CatalogStore._
import io.disposia.engine.domain._
import io.disposia.engine.searcher.Searcher.{SearcherRequest, SearcherResults}
import io.disposia.engine.cnc.ReplFormatter.format

import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * A processor for interactive commands. This is not a fully
  * fledged REPL, since it does not print the evaluation results.
  *
  * @param bus
  * @param config
  * @param executionContext
  */
class ReplProcessor(bus: ActorRef, config: EngineConfig, executionContext: ExecutionContext) {

  private val log = Logger(getClass)

  private implicit val INTERNAL_TIMEOUT: Timeout = config.internalTimeout

  def eval(args: String): String = Option(args)
    .map(_.split(" "))
    .map(eval)
    .getOrElse("Input was NULL")

  def eval(args: Array[String]): String = Option(args)
    .map(_.toList)
    .map(eval)
    .getOrElse("Input was NULL")

  def eval(args: List[String]): String = Option(args)
    .map {
      case "help" :: _ => help()
      //case q@("q" | "quit" | "exit") :: _ => shutdown = true

      case "propose" :: Nil => usage("propose")
      case "propose" :: feeds => propose(feeds)

      case "search" :: Nil => usage("search")
      case "search" :: query :: Nil => search(query)
      //case "search" :: query :: _   => usage("search")

      case "get" :: "podcast" :: Nil => usage("get podcast")
      case "get" :: "podcast" :: id :: Nil => getPodcast(id)
      case "get" :: "podcast" :: id :: _ => usage("get podcast")

      case "get" :: "podcast-feeds" :: Nil => usage("get podcast")
      case "get" :: "podcast-feeds" :: id :: Nil => getFeedsByPodcast(id)
      case "get" :: "podcast-feeds" :: id :: _ => usage("get podcast")

      case "get" :: "episode" :: Nil => usage("get episode")
      case "get" :: "episode" :: id :: Nil => getEpisode(id)
      case "get" :: "episode" :: id :: _ => usage("get episode")

      case "get" :: "episode-chapters" :: Nil => usage("get chapters")
      case "get" :: "episode-chapters" :: id :: Nil => getChaptersByEpisode(id)
      case "get" :: "episode-chapters" :: id :: _ => usage("get chapters")

      case _ => help()
    }.getOrElse("Input was NULL")

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

  private def search(query: String): String = resolveResponse(bus ? SearcherRequest(query, config.indexConfig.defaultPage, config.indexConfig.defaultSize))

  private def getPodcast(id: String): String = resolveResponse(bus ? GetPodcast(id))

  private def getEpisode(id: String): String = resolveResponse(bus ? GetEpisode(id))

  private def getFeed(id: String): String = resolveResponse(bus ? GetFeed(id))

  private def getEpisodesByPodcast(id: String): String = resolveResponse(bus ? GetEpisodesByPodcast(id))

  private def getFeedsByPodcast(id: String): String = resolveResponse(bus ? GetFeedsByPodcast(id))

  private def getChaptersByEpisode(id: String): String = resolveResponse(bus ? GetChaptersByEpisode(id))

  private def resolveResponse(option: Option[Any]): String = option match {
    case Some(p: Podcast) => format(p)
    case Some(e: Episode) => format(e)
    case Some(f: Feed)    => format(f)
    case Some(c: Chapter) => format(c)
    case Some(i: Image)   => format(i)
    case Some(other) => unhandled(other)
    case None  => "No database record found"

  }

  private def resolveResponse(future: Future[Any]): String = Await.result(future, INTERNAL_TIMEOUT.duration) match {
    case PodcastResult(p)            => resolveResponse(p)
    case EpisodeResult(e)            => resolveResponse(e)
    case FeedResult(f)               => resolveResponse(f)
    case SearcherResults(rs)         => format(rs)
    case EpisodesByPodcastResult(es) => format(es)
    case FeedsByPodcastResult(fs)    => format(fs)
    case ChaptersByEpisodeResult(cs) => format(cs)

    case other => unhandled(other)
  }

  private def unhandled(unhandled: Any): String = {
    val msg = s"CLI has no specific handler for type : ${unhandled.getClass}"
    log.error(msg)
    msg // return to frontend (CLI/Web)
  }

}
