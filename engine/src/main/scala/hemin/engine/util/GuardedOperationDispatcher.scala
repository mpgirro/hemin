package hemin.engine.util

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import hemin.engine.HeminConfig
import hemin.engine.catalog.CatalogStore
import hemin.engine.cli.CommandLineInterpreter
import hemin.engine.model._
import hemin.engine.searcher.Searcher

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


class GuardedOperationDispatcher(bus: ActorRef,
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
      .onOpen(log.info("Circuit Breaker is open"))
      .onClose(log.warn("Circuit Breaker is closed"))
      .onHalfOpen(log.info("Circuit Breaker is half-open, next message goes through")))
      .get

  private def guarded[T](body: => Future[T]): Future[T] = circuitBreaker.withCircuitBreaker(body)

  private def guarded[T](body: => T): Try[T] = Try(circuitBreaker.withSyncCircuitBreaker(body))

  def checkPodcast(id: String): Unit = guarded {
    bus ? CatalogStore.CheckPodcast(id)
  }

  def cli(input: String): Future[String] = guarded {
    (bus ? CommandLineInterpreter.InterpreterInput(input))
      .mapTo[CommandLineInterpreter.InterpreterOutput]
      .map(_.output)
  }

  def getAllEpisodesByLatest(pageNumber: Option[Int], pageSize: Option[Int]): Future[List[Episode]] = guarded {
    (bus ? CatalogStore.GetLatestEpisodes(pageNumber, pageSize))
      .mapTo[CatalogStore.LatestEpisodesResult]
      .map(_.episodes)
  }

  def getAllPodcasts(pageNumber: Option[Int], pageSize: Option[Int]): Future[List[Podcast]] = guarded {
    (bus ? CatalogStore.GetAllPodcastsRegistrationComplete(pageNumber, pageSize))
      .mapTo[CatalogStore.AllPodcastsResult]
      .map(_.podcasts)
  }

  def getAllPodcastsByNewest(pageNumber: Option[Int], pageSize: Option[Int]): Future[List[Podcast]] = guarded {
    (bus ? CatalogStore.GetNewestPodcasts(pageNumber, pageSize))
      .mapTo[CatalogStore.NewestPodcastsResult]
      .map(_.podcasts)
  }

  def getDatabaseStats: Future[DatabaseStats] = guarded {
    (bus ? CatalogStore.GetDatabaseStats())
      .mapTo[CatalogStore.DatabaseStatsResult]
      .map(_.stats)
  }

  def getDistinctCategories: Future[Set[String]] = guarded {
    (bus ? CatalogStore.GetCategories())
      .mapTo[CatalogStore.CategoriesResult]
      .map(_.categories)
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

  def getImage(id: String): Future[Option[Image]] = guarded {
    (bus ? CatalogStore.GetImage(id))
      .mapTo[CatalogStore.ImageResult]
      .map(_.image)
  }

  def getPodcast(id: String): Future[Option[Podcast]] = guarded {
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

  def proposeFeed(url: String): Unit = guarded {
    bus ! CatalogStore.ProposeNewFeed(url)
  }

}
