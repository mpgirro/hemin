package hemin.api.v1.service

import io.hemin.engine.model._
import javax.inject.Inject
import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}

/**
  * Controls access to the backend data
  */
class EpisodeService @Inject()(engineService: EngineService)
                              (implicit ec: ExecutionContext) {

  private val engine = engineService.engine

  def find(id: String)(implicit mc: MarkerContext): Future[Option[Episode]] = engine.findEpisode(id)

  def latest(pageNumber: Option[Int], pageSize: Option[Int])(implicit mc: MarkerContext): Future[List[Episode]] =
    engine.findLatestEpisodes(pageNumber, pageSize)

  def chapters(id: String)(implicit mc: MarkerContext): Future[List[Chapter]] = engine.findChaptersByEpisode(id)

}
