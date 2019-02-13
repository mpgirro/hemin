package hemin.api.v1.service

import hemin.engine.model._
import javax.inject.Inject
import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}

/**
  * Controls access to the backend data
  */
class PodcastService @Inject()(engineService: EngineService)
                              (implicit ec: ExecutionContext) {

  private val engine = engineService.engine

  def find(id: String)(implicit mc: MarkerContext): Future[Option[Podcast]] =
    engine.findPodcast(id)

  def all(p: Option[Int], s: Option[Int])(implicit mc: MarkerContext): Future[List[Podcast]] =
    engine.findAllPodcasts(p, s)

  def newest(pageNumber: Option[Int], pageSize: Option[Int])(implicit mc: MarkerContext): Future[List[Podcast]] =
    engine.findNewestPodcasts(pageNumber, pageSize)

  def episodes(id: String)(implicit mc: MarkerContext): Future[List[Episode]] =
    engine.findEpisodesByPodcast(id)

  def feeds(id: String)(implicit mc: MarkerContext): Future[List[Feed]] =
    engine.findFeedsByPodcast(id)

}
