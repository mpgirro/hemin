package io.hemin.engine.node

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import io.hemin.engine.HeminConfig
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.model._
import io.hemin.engine.searcher.Searcher
import io.hemin.engine.util.cli.CommandLineInterpreter

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


class InternalOperationDispatcher(bus: ActorRef,
                                  system: ActorSystem,
                                  config: HeminConfig,
                                  ec: ExecutionContext) {

  private val log = Logger(getClass)

  private implicit val executionContext: ExecutionContext = ec
  private implicit val internalTimeout: Timeout = config.node.internalTimeout

  private lazy val circuitBreaker: CircuitBreaker =
    (for {
      scheduler    <- Option(system).map(_.scheduler)
      maxFailures  <- Option(config).map(_.node.breakerMaxFailures)
      callTimeout  <- Option(config).map(_.node.breakerCallTimeout.duration)
      resetTimeout <- Option(config).map(_.node.breakerResetTimeout.duration)
    } yield CircuitBreaker(scheduler, maxFailures, callTimeout, resetTimeout)
      .onOpen(breakerOpen())
      .onClose(breakerClose())
      .onHalfOpen(breakerHalfOpen())).get

  private def breakerOpen(): Unit = log.info("Circuit Breaker is open")

  private def breakerClose(): Unit = log.warn("Circuit Breaker is closed")

  private def breakerHalfOpen(): Unit = log.info("Circuit Breaker is half-open, next message goes through")

  private def guarded[T](body: => Future[T]): Future[T] = circuitBreaker.withCircuitBreaker(body)

  private def guarded[T](body: => T): Try[T] = Try(circuitBreaker.withSyncCircuitBreaker(body))

  def checkPodcast(id: String): Future[String] = Future {
    bus ? CatalogStore.CheckPodcast(id)
    "Attempting to check podcast" // we need this result type
  }

  def cli(input: String): Future[String] = guarded {
    (bus ? CommandLineInterpreter.InterpreterInput(input))
      .mapTo[CommandLineInterpreter.InterpreterOutput]
      .map(_.output)
  }

  def getEpisode(id: String): Future[Option[Episode]] = guarded {
    (bus ? CatalogStore.GetEpisode(id))
      .mapTo[CatalogStore.EpisodeResult]
      .map(_.episode)
  }

  def getEpisodeChapters(id: String): Future[List[Chapter]] = guarded {
    (bus ? CatalogStore.GetChaptersByEpisode(id))
      .mapTo[CatalogStore.ChaptersByEpisodeResult]
      .map(_.chapters)
  }

  def getFeeds(id: String): Future[Option[Feed]] = guarded {
    (bus ? CatalogStore.GetFeed(id))
      .mapTo[CatalogStore.FeedResult]
      .map(_.feed)
  }

  def getPodcast(id: String): Future[Option[Podcast]] = {
    (bus ? CatalogStore.GetPodcast(id))
      .mapTo[CatalogStore.PodcastResult]
      .map(_.podcast)
  }

  def getPodcastEpisodes(id: String): Future[List[Episode]] = guarded {
    (bus ? CatalogStore.GetEpisodesByPodcast(id))
      .mapTo[CatalogStore.EpisodesByPodcastResult]
      .map(_.episodes)
  }

  def getPodcastFeeds(id: String): Future[List[Feed]] = guarded {
    (bus ? CatalogStore.GetFeedsByPodcast(id))
      .mapTo[CatalogStore.FeedsByPodcastResult]
      .map(_.feeds)
  }

  def getSearchResult(query: String, pageNumber: Option[Int], pageSize: Option[Int]): Future[SearchResult] = guarded {
    (bus ? Searcher.SearchRequest(query, pageNumber, pageSize))
      .mapTo[Searcher.SearchResults]
      .map(_.results)
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
