package io.hemin.engine.util.cli

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import io.hemin.engine.HeminConfig
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.searcher.Searcher

import scala.concurrent.{ExecutionContext, Future}


class InternalProcess(bus: ActorRef,
                      config: HeminConfig,
                      ec: ExecutionContext) {

  private implicit val executionContext: ExecutionContext = ec
  private implicit val internalTimeout: Timeout = config.node.internalTimeout

  def checkPodcast(id: String): Future[String] = Future {
    bus ? CatalogStore.CheckPodcast(id)
    "Attempting to check podcast" // we need this result type
  }

  def getEpisode(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetEpisode(id))

  def getEpisodeChapters(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetChaptersByEpisode(id))

  def getFeeds(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetFeed(id))

  def getPodcast(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetPodcast(id))

  def getPodcastEpisodes(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetEpisodesByPodcast(id))

  def getPodcastFeeds(id: String): Future[String] =
    CliFormatter.cliResult(bus ? CatalogStore.GetFeedsByPodcast(id))

  def getSearchResult(words: List[String]): Future[String] = {
    val query: String = words.mkString(" ")
    val pageNumber: Option[Int] = Some(config.searcher.defaultPage)
    val pageSize: Option[Int] = Some(config.searcher.defaultSize)
    CliFormatter.cliResult(bus ? Searcher.SearchRequest(query, pageNumber, pageSize))
  }

  def proposeFeed(urls: List[String]): Future[String] = Future {
    val out = new StringBuilder
    urls.foreach { f =>
      out ++= "proposing " + f
      bus ! CatalogStore.ProposeNewFeed(f)
    }
    out.mkString
  }

}
