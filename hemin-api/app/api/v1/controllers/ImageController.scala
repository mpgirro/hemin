package api.v1.controllers

import api.v1.controllers.bases.ImageBaseController
import api.v1.controllers.components.ImageControllerComponents
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

class ImageController @Inject() (cc: ImageControllerComponents)
  extends ImageBaseController(cc) {

  private val log = Logger(getClass).logger

  def find(id: String): Action[AnyContent] =
    ImageAction.async { implicit request =>
      log.trace(s"GET image: id = $id")
      imageService
        .find(id)
        .map {
          case Some(i) => Ok(Json.toJson(i))
          case None    => NotFound
        }
    }

}
