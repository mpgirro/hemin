package api.v1.search

import io.hemin.engine.domain.ResultsWrapper
import javax.inject.Inject
import play.api.MarkerContext
import services.EngineService

import scala.concurrent.{ExecutionContext, Future}

/**
  * Controls access to the backend data
  */
class SearchService @Inject()(engineService: EngineService)
                             (implicit ec: ExecutionContext) {

  private val engine = engineService.engine

  def search(query: String, page: Option[Int], size: Option[Int])(implicit mc: MarkerContext): Future[ResultsWrapper] =
    engine.search(query, page, size)

}
