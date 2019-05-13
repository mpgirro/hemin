package io.hemin.api.v1.rest

import io.hemin.api.v1.rest.base.ImageBaseController
import io.hemin.api.v1.rest.component.ImageControllerComponents
import io.hemin.engine.model.Image
import io.swagger.annotations._
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

@Api("Image")
class ImageController @Inject() (cc: ImageControllerComponents)
  extends ImageBaseController(cc) {

  private val log = Logger(getClass).logger

  @ApiOperation(
    value    = "Finds an Image by ID",
    response = classOf[Image])
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid ID supplied"),
    new ApiResponse(code = 404, message = "Image not found")))
  def find(
    @ApiParam(value = "ID of the Image") id: String): Action[AnyContent] = ImageAction.async {
    implicit request =>
      log.trace(s"GET image: id = $id")
      imageService
        .find(id)
        .map {
          case Some(i) => Ok(Json.toJson(i))
          case None    => NotFound
        }
    }

}
