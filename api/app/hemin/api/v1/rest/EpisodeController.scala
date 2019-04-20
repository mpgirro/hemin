package hemin.api.v1.rest

import hemin.api.v1.rest.base.EpisodeBaseController
import hemin.api.v1.rest.component.EpisodeControllerComponents
import hemin.api.v1.util.ArrayWrapper
import hemin.engine.model.{Chapter, Episode}
import io.swagger.annotations._
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

@Api("Episode")
class EpisodeController @Inject() (cc: EpisodeControllerComponents)
  extends EpisodeBaseController(cc) {

  private val log = Logger(getClass).logger

  @ApiOperation(
    value    = "Finds an Episode by ID",
    response = classOf[Episode])
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid ID supplied"),
    new ApiResponse(code = 404, message = "Episode not found")))
  def find(
    @ApiParam(value = "ID of the Episode to fetch") id: String): Action[AnyContent] = EpisodeAction.async {
    implicit request =>
      log.trace(s"GET episode: id = $id")
      episodeService
        .find(id)
        .map {
          case Some(e) => Ok(Json.toJson(e))
          case None    => NotFound
        }
    }

  @ApiOperation(
    value    = "Finds Chapters of an Episode by ID",
    response = classOf[Chapter],
    responseContainer = "List")
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid ID supplied"),
    new ApiResponse(code = 404, message = "Episode not found")))
  def chapters(
    @ApiParam(value = "ID of the Episode") id: String): Action[AnyContent] = EpisodeAction.async {
    implicit request =>
      log.trace(s"GET chapters by episode: id = $id")
      episodeService
        .chapters(id)
        .map(cs => Ok(Json.toJson(ArrayWrapper(cs))))
    }

  @ApiOperation(
    value    = "Finds latest Episodes registered by Hemin",
    response = classOf[Episode],
    responseContainer = "List")
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid ID supplied")))
  def latest(
    @ApiParam(value = "Number of the page of Episodes to fetch") pageNumber: Option[Int],
    @ApiParam(value = "Size of the page of Episodes to fetch") pageSize: Option[Int]): Action[AnyContent] = EpisodeAction.async {
    implicit request =>
      log.trace(s"GET latest episodes: pageNumber = $pageNumber & pageSize = $pageSize")

      /* TODO produce useful status codes for invalid arguments
      if (pageNumber.isDefined && pageNumber.get < 0) {
        return BadRequest
      }

      if (pageSize.isDefined && pageSize.get < 0) {
        return BadRequest
      }
      */

      episodeService
        .latest(pageNumber, pageSize)
        .map(es => Ok(Json.toJson(ArrayWrapper(es))))
    }

}
