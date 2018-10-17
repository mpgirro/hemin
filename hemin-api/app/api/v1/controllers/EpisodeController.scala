package api.v1.controllers

import api.v1.controllers.bases.EpisodeBaseController
import api.v1.controllers.components.EpisodeControllerComponents
import api.v1.utils.ArrayWrapper
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

class EpisodeController @Inject() (cc: EpisodeControllerComponents)
  extends EpisodeBaseController(cc) {

  private val log = Logger(getClass).logger

  def find(id: String): Action[AnyContent] =
    EpisodeAction.async { implicit request =>
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

}
