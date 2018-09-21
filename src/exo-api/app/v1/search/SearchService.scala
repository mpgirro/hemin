package v1.search

import com.google.common.base.Strings.isNullOrEmpty
import com.typesafe.config.ConfigFactory
import exo.engine.domain.dto.ResultWrapper
import javax.inject.{Inject, Provider}
import play.api.MarkerContext
import services.EngineService

import scala.concurrent.{ExecutionContext, Future}

// TODO das hier ist ein Service

/**
  * Controls access to the backend data, returning [[exo.engine.domain.dto.ResultWrapper]]
  */
class SearchService @Inject()(engineService: EngineService)
                             (implicit ec: ExecutionContext) {

    // TODO dont use CONFIG this way, and defauts via Option doesn't work anyway
    private val CONFIG = ConfigFactory.load()
    private val DEFAULT_PAGE: Int = Option(CONFIG.getInt("search.default-page")).getOrElse(1)
    private val DEFAULT_SIZE: Int = Option(CONFIG.getInt("search.default-size")).getOrElse(20)

    private val engine = engineService.engine

    def search(query: String, page: Option[Int], size: Option[Int])(implicit mc: MarkerContext): Future[ResultWrapper] = {

        val p: Int = page.getOrElse(DEFAULT_PAGE)
        val s: Int = size.getOrElse(DEFAULT_SIZE)

        if (isNullOrEmpty(query)) {
            return Future { ResultWrapper.empty() }
        }

        if (p < 1) {
            return Future { ResultWrapper.empty() }
        }

        if (s < 1) {
            return Future { ResultWrapper.empty() }
        }

        engine.search(query, p, s)
    }

}
