package api.v1.controllers

import api.v1.controllers.bases.FeedBaseController
import api.v1.controllers.components.FeedControllerComponents
import io.swagger.annotations.Api
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future

@Api("Feed")
class FeedController @Inject() (cc: FeedControllerComponents)
  extends FeedBaseController(cc) {

  private val log = Logger(getClass).logger

  def find(id: String): Action[AnyContent] =
    FeedAction.async { implicit request =>
      log.trace(s"GET feed: id = $id")
      feedService
        .find(id)
        .map {
          case Some(f) => Ok(Json.toJson(f))
          case None    => NotFound
        }
    }

  def propose: Action[AnyContent] =
    FeedAction.async { implicit request =>
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

}
