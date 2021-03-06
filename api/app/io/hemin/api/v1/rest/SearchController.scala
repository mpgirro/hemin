package io.hemin.api.v1.rest

import io.hemin.api.v1.rest.base.SearchBaseController
import io.hemin.api.v1.rest.component.SearchControllerComponents
import io.hemin.engine.model.SearchResult
import io.swagger.annotations._
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
  def search(
    @ApiParam(value = "Query to search for") query: String,
    @ApiParam(value = "Number of the page of search results") pageNumber: Option[Int],
    @ApiParam(value = "Size of the page of search results") pageSize: Option[Int]): Action[AnyContent] = SearchAction.async {
    implicit request =>
      val p: String = pageNumber.map("& p = "+_).getOrElse("")
      val s: String = pageSize.map(" & s = "+_).getOrElse("")
      log.trace(s"SEARCH: q = $query $p $s")

      // TODO check that pageNumber / pageSize are > 1

      searchService
        .search(query,pageNumber,pageSize)
        .map(rs => Ok(Json.toJson(rs)))
    }
}
