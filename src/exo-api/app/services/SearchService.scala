package services

import exo.engine.domain.dto.ResultWrapper
import javax.inject.{Inject, Singleton}

import scala.concurrent.Future

/**
  * @author max
  */
@Singleton
class SearchService @Inject()(engineService: EngineService) {

    private val engine = engineService.engine

    def search(query: String, page: Int, size: Int): Future[ResultWrapper] = {
        engine.search(query, page, size)
    }

}
