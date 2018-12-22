package api.v1.services

import io.hemin.engine.model.SearchResult
import javax.inject.Inject
import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}

/**
  * Controls access to the backend data
  */
class SearchService @Inject()(engineService: EngineService)
                             (implicit ec: ExecutionContext) {

  private val engine = engineService.engine

  def search(query: String, page: Option[Int], size: Option[Int])(implicit mc: MarkerContext): Future[SearchResult] =
    engine.search(query, page, size)

}
