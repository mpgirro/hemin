package hemin.api.v1.rest

import hemin.api.v1.rest.base.EpisodeBaseController
import hemin.api.v1.rest.component.EpisodeControllerComponents
import hemin.api.v1.util.ArrayWrapper
import hemin.engine.model.Episode
import io.swagger.annotations._
import javax.inject.Inject
import javax.ws.rs.{ GET, Path, PathParam, Produces }
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

@Path("/episode")
@Api("Episode")
class EpisodeController @Inject() (cc: EpisodeControllerComponents)
  extends EpisodeBaseController(cc) {

  private val log = Logger(getClass).logger

  @GET
  @Path("/")
  @ApiOperation(value = "Finds an Episode by ID",
    notes = "Multiple status values can be provided with comma seperated strings",
    response = classOf[Episode],
  responseContainer = "List")
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

  def chapters(id: String): Action[AnyContent] =
    EpisodeAction.async { implicit request =>
      log.trace(s"GET chapters by episode: id = $id")
      episodeService
        .chapters(id)
        .map { cs =>
          Ok(Json.toJson(ArrayWrapper(cs)))
        }
    }

  def latest(pageNumber: Option[Int], pageSize: Option[Int]): Action[AnyContent] =
    EpisodeAction.async { implicit request =>
      log.trace(s"GET latest episodes: pageNumber = $pageNumber & pageSize = $pageSize")
      episodeService
        .latest(pageNumber, pageSize)
        .map(es => Ok(Json.toJson(ArrayWrapper(es))))
    }

}
