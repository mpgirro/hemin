package api.v1.controllers

import api.v1.controllers.bases.SearchBaseController
import api.v1.controllers.components.SearchControllerComponents
import io.swagger.annotations.Api
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

@Api
class SearchController @Inject() (cc: SearchControllerComponents)
  extends SearchBaseController(cc) {

  private val log = Logger(getClass).logger

  def search(q: String, p: Option[Int], s: Option[Int]): Action[AnyContent] =
    SearchAction.async { implicit request =>
      log.trace(s"SEARCH: q = $q ${p.map("& p = "+_).getOrElse("")} ${s.map(" & s = "+_).getOrElse("")}")
      searchService
        .search(q,p,s)
        .map { rs =>
          Ok(Json.toJson(rs))
        }
    }
}
