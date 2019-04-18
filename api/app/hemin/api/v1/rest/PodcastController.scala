package hemin.api.v1.rest

import hemin.api.v1.rest.base.PodcastBaseController
import hemin.api.v1.rest.component.PodcastControllerComponents
import hemin.api.v1.util.ArrayWrapper
import hemin.engine.model.{Episode, Podcast}
import hemin.engine.util.mapper.{EpisodeMapper, PodcastMapper}
import io.swagger.annotations._
import javax.inject.Inject
import javax.ws.rs.{GET, Path}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

@Api("Podcast")
@Path("/api/v1/podcast")
class PodcastController @Inject() (cc: PodcastControllerComponents)
  extends PodcastBaseController(cc) {

  private val log = Logger(getClass).logger

  @GET
  @Path("/")
  @ApiOperation(
    value    = "Finds an Podcast by ID",
    notes    = "Multiple status values can be provided with comma seperated strings",
    response = classOf[Podcast])
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid ID supplied"),
    new ApiResponse(code = 404, message = "Podcast not found")))
  def find(
    @ApiParam(value = "ID of the Podcast to fetch") id: String): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET podcast: id = $id")
      podcastService
        .find(id)
        .map {
          case Some(p) => Ok(Json.toJson(p))
          case None    => NotFound
        }
    }

  @GET
  @Path("/")
  @ApiOperation(
    value             = "Finds Podcasts by Page-Number and Page-Size",
    response          = classOf[Podcast],
    responseContainer = "List")
  def all(p: Option[Int], s: Option[Int]): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET all podcasts: p = $p & s = $s")
      podcastService
        .all(p, s)
        .map { ps =>
          Ok(Json.toJson(ArrayWrapper(ps)))
        }
    }

  def allAsTeaser(p: Option[Int], s: Option[Int]): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET all podcasts as teasers: p = $p & s = $s")
      podcastService
        .all(p, s)
        .map(ps => ps.map(PodcastMapper.toTeaser))
        .map(_.flatten)
        .map { ps =>
          Ok(Json.toJson(ArrayWrapper(ps)))
        }
    }

  def episodes(id: String): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET episodes by podcast (reduced to teasers): id = $id")
      podcastService
        .episodes(id)
        .map(_.map(EpisodeMapper.toTeaser))
        .map(_.flatten)
        .map { es =>
          Ok(Json.toJson(ArrayWrapper(es)))
        }
    }

  def feeds(id: String): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET feeds by podcast: id = $id")
      podcastService
        .feeds(id)
        .map { fs =>
          Ok(Json.toJson(ArrayWrapper(fs)))
        }
    }

  def newest(pageNumber: Option[Int], pageSize: Option[Int]): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET newest podcasts: pageNumber = $pageNumber & pageSize = $pageSize")
      podcastService
        .newest(pageNumber, pageSize)
        .map(ps => Ok(Json.toJson(ArrayWrapper(ps))))
    }
}
