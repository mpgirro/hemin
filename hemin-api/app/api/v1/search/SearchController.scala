package api.v1.search

import io.hemin.engine.domain.ResultsWrapper
import javax.inject.Inject
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import util.JsonWrites

import scala.concurrent.ExecutionContext

class SearchController @Inject() (cc: SearchControllerComponents,
                                  searchActionBuilder: SearchActionBuilder,
                                  searchService: SearchService,
                                  actionBuilder: DefaultActionBuilder,
                                  parsers: PlayBodyParsers,
                                  messagesApi: MessagesApi,
                                  langs: Langs,
                                  fileMimeTypes: FileMimeTypes)
                                 (implicit ec: ExecutionContext)
  extends SearchBaseController(cc) {

  private val log = Logger(getClass).logger

  private implicit val searchWriter: Writes[ResultsWrapper] = JsonWrites.implicitWrapperWrites

  def search(q: String, p: Option[Int], s: Option[Int]): Action[AnyContent] =
    SearchAction.async { implicit request =>
      log.trace(s"SEARCH: q = $q & p = $p & s = $s")
      searchService
        .search(q,p,s)
        .map { rs =>
          Ok(Json.toJson(rs))
        }
    }
}
