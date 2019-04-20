package hemin.api.v1.rest

import hemin.api.v1.rest.base.PodcastBaseController
import hemin.api.v1.rest.component.PodcastControllerComponents
import hemin.api.v1.util.ArrayWrapper
import hemin.engine.model.{Episode, Feed, Podcast}
import hemin.engine.util.mapper.{EpisodeMapper, PodcastMapper}
import io.swagger.annotations._
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

@Api("Podcast")
class PodcastController @Inject() (cc: PodcastControllerComponents)
  extends PodcastBaseController(cc) {

  private val log = Logger(getClass).logger

  @ApiOperation(
    value    = "Finds an Podcast by ID",
    notes    = "Multiple status values can be provided with comma seperated strings",
    response = classOf[Podcast])
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid ID supplied"),
    new ApiResponse(code = 404, message = "Podcast not found")))
  def find(
    @ApiParam(value = "ID of the Podcast to fetch") id: String): Action[AnyContent] = PodcastAction.async {
    implicit request =>
      log.trace(s"GET podcast: id = $id")
      podcastService
        .find(id)
        .map {
          case Some(p) => Ok(Json.toJson(p))
          case None    => NotFound
        }
    }

  @ApiOperation(
    value             = "Finds Podcasts by Page-Number and Page-Size",
    response          = classOf[Podcast],
    responseContainer = "List")
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Page-Number or Page-Size smaller than 1")))
  def all(
    @ApiParam(value = "Number of the page of Podcasts to fetch") pageNumber: Option[Int],
    @ApiParam(value = "Size of the page of Podcasts to fetch") pageSize: Option[Int]): Action[AnyContent] = PodcastAction.async {
    implicit request =>
      log.trace(s"GET all podcasts: p = $pageNumber & s = $pageSize")

      // TODO check that pageNumber / pageSize are > 1

      podcastService
        .all(pageNumber, pageSize)
        .map(ps => Ok(Json.toJson(ArrayWrapper(ps))))
    }

  @ApiOperation(
    value             = "Finds Podcasts by Page-Number and Page-Size in a condensed information density",
    response          = classOf[Podcast],
    responseContainer = "List")
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Page-Number or Page-Size smaller than 1")))
  def allAsTeaser(
    @ApiParam(value = "Number of the page of Podcasts to fetch") pageNumber: Option[Int],
    @ApiParam(value = "Size of the page of Podcasts to fetch") pageSize: Option[Int]): Action[AnyContent] = PodcastAction.async {
    implicit request =>
      log.trace(s"GET all podcasts as teasers: pageNumber = $pageNumber & pageSize = $pageSize")

      // TODO check that pageNumber / pageSize are > 1

      podcastService
        .all(pageNumber, pageSize)
        .map(ps => ps.map(PodcastMapper.toTeaser))
        .map(_.flatten)
        .map(ps => Ok(Json.toJson(ArrayWrapper(ps))))
    }

  @ApiOperation(
    value    = "Finds Episodes of a Podcast by ID",
    response = classOf[Episode],
    responseContainer = "List")
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid ID supplied"),
    new ApiResponse(code = 404, message = "Podcast not found")))
  def episodes(
    @ApiParam(value = "ID of the Podcast") id: String): Action[AnyContent] = PodcastAction.async {
    implicit request =>
      log.trace(s"GET episodes by podcast (reduced to teasers): id = $id")
      podcastService
        .episodes(id)
        .map(_.map(EpisodeMapper.toTeaser))
        .map(_.flatten)
        .map(es => Ok(Json.toJson(ArrayWrapper(es))))
    }

  @ApiOperation(
    value    = "Finds Feeds of a Podcast by ID",
    response = classOf[Feed],
    responseContainer = "List")
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid ID supplied"),
    new ApiResponse(code = 404, message = "Podcast not found")))
  def feeds(
    @ApiParam(value = "ID of the Podcast") id: String): Action[AnyContent] = PodcastAction.async {
    implicit request =>
      log.trace(s"GET feeds by podcast: id = $id")
      podcastService
        .feeds(id)
        .map(fs => Ok(Json.toJson(ArrayWrapper(fs))))
    }

  @ApiOperation(
    value             = "Finds newest Podcasts registered by Hemin by Page-Number and Page-Size",
    response          = classOf[Podcast],
    responseContainer = "List")
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Page-Number or Page-Size smaller than 1")))
  def newest(
    @ApiParam(value = "Number of the page of Podcasts to fetch") pageNumber: Option[Int],
    @ApiParam(value = "Size of the page of Podcasts to fetch") pageSize: Option[Int]): Action[AnyContent] = PodcastAction.async {
    implicit request =>
      log.trace(s"GET newest podcasts: pageNumber = $pageNumber & pageSize = $pageSize")

      // TODO check that pageNumber / pageSize are > 1

      podcastService
        .newest(pageNumber, pageSize)
        .map(ps => Ok(Json.toJson(ArrayWrapper(ps))))
    }
}
