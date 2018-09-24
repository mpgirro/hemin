package api.v1.podcast

import com.google.common.base.Strings.isNullOrEmpty
import com.typesafe.config.ConfigFactory
import io.disposia.engine.domain.dto.{Episode, Feed, Podcast, ResultWrapper}
import javax.inject.{Inject, Provider}
import play.api.MarkerContext
import services.EngineService

import scala.concurrent.{ExecutionContext, Future}

// TODO das hier ist ein Service

/**
  * Controls access to the backend data, returning [[io.disposia.engine.domain.dto.Podcast]]
  */
class PodcastService @Inject()(engineService: EngineService)
                              (implicit ec: ExecutionContext) {

    private val engine = engineService.engine

    def find(id: String)(implicit mc: MarkerContext): Future[Option[Podcast]] = engine.findPodcast(id)

    def all(p: Option[Int], s: Option[Int])(implicit mc: MarkerContext): Future[List[Podcast]] = engine.findAllPodcasts(p, s)

    def episodes(id: String)(implicit mc: MarkerContext): Future[List[Episode]] = engine.findEpisodesByPodcast(id)

    def feeds(id: String)(implicit mc: MarkerContext): Future[List[Feed]] = engine.findFeedsByPodcast(id)

}
