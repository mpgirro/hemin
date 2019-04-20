package hemin.api.v1.rest

import hemin.api.v1.rest.base.SearchBaseController
import hemin.api.v1.rest.component.SearchControllerComponents
import hemin.engine.model.SearchResult
import io.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

@Api("Search")
class SearchController @Inject() (cc: SearchControllerComponents)
  extends SearchBaseController(cc) {

  private val log = Logger(getClass).logger

  @ApiOperation(
    value    = "Search for index results by query",
    response = classOf[SearchResult])
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid query, page number, or page size")))
  def search(query: String, pageNumber: Option[Int], pageSize: Option[Int]): Action[AnyContent] = SearchAction.async {
    implicit request =>
      val p: String = pageNumber.map("& p = "+_).getOrElse("")
      val s: String = pageSize.map(" & s = "+_).getOrElse("")
      log.trace(s"SEARCH: q = $query $p $s")
      searchService
        .search(query,pageNumber,pageSize)
        .map(rs => Ok(Json.toJson(rs)))
    }
}
