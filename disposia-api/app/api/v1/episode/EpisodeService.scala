package api.v1.episode

import com.google.common.base.Strings.isNullOrEmpty
import com.typesafe.config.ConfigFactory
import io.disposia.engine.domain._
import javax.inject.{Inject, Provider}
import play.api.MarkerContext
import services.EngineService

import scala.concurrent.{ExecutionContext, Future}

// TODO das hier ist ein Service

/**
  * Controls access to the backend data, returning [[io.disposia.engine.domain.Episode]]
  */
class EpisodeService @Inject()(engineService: EngineService)
                              (implicit ec: ExecutionContext) {

  private val engine = engineService.engine

  def find(id: String)(implicit mc: MarkerContext): Future[Option[Episode]] = engine.findEpisode(id)

  def chapters(id: String)(implicit mc: MarkerContext): Future[List[Chapter]] = engine.findChaptersByEpisode(id)

}
