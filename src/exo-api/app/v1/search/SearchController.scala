package v1.search

import exo.engine.domain.dto.ResultWrapper
import javax.inject.Inject
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import services.SearchService
import v1.RequestMarkerContext

import scala.concurrent.ExecutionContext


/**
  * @author max
  */
class SearchController @Inject() (cc: SearchControllerComponents,
                                  searchActionBuilder: SearchActionBuilder,
                                  searchResourceHandler: SearchResourceHandler,
                                  actionBuilder: DefaultActionBuilder,
                                  parsers: PlayBodyParsers,
                                  messagesApi: MessagesApi,
                                  langs: Langs,
                                  fileMimeTypes: FileMimeTypes,
                                  searchService: SearchService)
                                 (implicit ec: ExecutionContext)
    extends SearchBaseController(cc) {

    private val log = Logger(getClass).logger

    private implicit val searchWriter: Writes[ResultWrapper] = ResultWrapperWrites.implicitWrapperWrites

    def search(q: String, p: Int, s: Int): Action[AnyContent] = SearchAction.async { implicit request =>
        log.trace(s"search: q = $q & p = $p & s = $s")
        searchResourceHandler.search(q,p,s).map { results =>
            Ok(Json.toJson(results))
        }
    }
}
