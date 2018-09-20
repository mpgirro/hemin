package v1.search

import exo.engine.domain.dto.ResultWrapper
import javax.inject.{Inject, Provider}
import play.api.MarkerContext
import services.EngineService

import scala.concurrent.{ExecutionContext, Future}

// TODO das hier ist ein Service

/**
  * Controls access to the backend data, returning [[exo.engine.domain.dto.ResultWrapper]]
  */
class SearchResourceHandler @Inject() (routerProvider: Provider[SearchRouter],
                                       engineService: EngineService)
                                      (implicit ec: ExecutionContext) {

    private val engine = engineService.engine

    def search(q: String, p: Int, s: Int)(implicit mc: MarkerContext): Future[ResultWrapper] = {

        engine.search(q, s, p)
        /*
        val resultFuture = engine.search(q, s, p)
        resultFuture.map { maybeResultData =>
            maybeResultData.map { resultData =>
                resultData
            }
        }
        */
    }

    /*
    def find(implicit mc: MarkerContext): Future[Iterable[ResultWrapper]] = {
        postRepository.list().map { postDataList =>
            postDataList.map(postData => createPostResource(postData))
        }
    }
    */

}
