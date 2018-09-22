package v1.podcast

import com.google.common.base.Strings.isNullOrEmpty
import com.typesafe.config.ConfigFactory
import io.disposia.engine.domain.dto.{Podcast, ResultWrapper}
import javax.inject.{Inject, Provider}
import play.api.MarkerContext
import services.EngineService

import scala.concurrent.{ExecutionContext, Future}

// TODO das hier ist ein Service

/**
  * Controls access to the backend data, returning [[io.disposia.engine.domain.dto.ResultWrapper]]
  */
class PodcastService @Inject()(engineService: EngineService)
                              (implicit ec: ExecutionContext) {

    private val engine = engineService.engine

    def find(id: String)(implicit mc: MarkerContext): Future[Option[Podcast]] = engine.findPodcast(id)

}
