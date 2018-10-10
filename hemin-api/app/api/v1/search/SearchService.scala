package api.v1.search

import com.typesafe.config.ConfigFactory
import io.hemin.engine.domain.ResultsWrapper
import javax.inject.Inject
import play.api.MarkerContext
import services.EngineService

import scala.concurrent.{ExecutionContext, Future}

// TODO das hier ist ein Service

/**
  * Controls access to the backend data, returning [[io.hemin.engine.domain.ResultsWrapper]]
  */
class SearchService @Inject()(engineService: EngineService)
                             (implicit ec: ExecutionContext) {

  // TODO dont use CONFIG this way, and defauts via Option doesn't work anyway
  private val CONFIG = ConfigFactory.load()
  private val DEFAULT_PAGE: Int = Option(CONFIG.getInt("search.default-page")).getOrElse(1)
  private val DEFAULT_SIZE: Int = Option(CONFIG.getInt("search.default-size")).getOrElse(20)

  private val engine = engineService.engine

  def search(query: String, page: Option[Int], size: Option[Int])(implicit mc: MarkerContext): Future[ResultsWrapper] =
    search(query, page.getOrElse(DEFAULT_PAGE), size.getOrElse(DEFAULT_SIZE))

  private def search(q: String, p: Int, s: Int)(implicit mc: MarkerContext): Future[ResultsWrapper] =
    engine.search(q, p, s)

}
