package io.hemin.api.v1.rest

import io.hemin.api.v1.rest.base.FeedBaseController
import io.hemin.api.v1.rest.component.FeedControllerComponents
import io.hemin.engine.model.Feed
import io.swagger.annotations._
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future

@Api("Feed")
class FeedController @Inject() (cc: FeedControllerComponents)
  extends FeedBaseController(cc) {

  private val log = Logger(getClass).logger

  @ApiOperation(
    value    = "Finds an Feed by ID",
    response = classOf[Feed])
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid ID supplied"),
    new ApiResponse(code = 404, message = "Feed not found")))
  def find(
    @ApiParam(value = "ID of the Feed") id: String): Action[AnyContent] = FeedAction.async {
    implicit request =>
      log.trace(s"GET feed: id = $id")
      feedService
        .find(id)
        .map {
          case Some(f) => Ok(Json.toJson(f))
          case None    => NotFound
        }
    }

  def propose: Action[AnyContent] = FeedAction.async {
    implicit request =>
      Future {
        request.body.asText match {
          case Some(url) =>
            log.trace(s"PROPOSE feed: $url")
            feedService.propose(url)
            Ok
          case None =>
            log.warn(s"PROPOSE feed: No URL in body [BadRequest]")
            BadRequest
        }
      }
    }

  def opmlImport: Action[AnyContent] = FeedAction.async {
    implicit request =>
      Future {
        request.body.asText match {
          case Some(xmlData) =>
            log.trace(s"OPML IMPORT")
            feedService.opmlImport(xmlData)
            Ok
          case None =>
            log.warn(s"OPML IMPORT: XML in body missing [BadRequest]")
            BadRequest
        }
      }
    }

}
