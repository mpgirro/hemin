package api.v1.controllers

import api.v1.actions.ImageActionBuilder
import api.v1.services.ImageService
import io.hemin.engine.domain._
import javax.inject.Inject
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import api.v1.utils.JsonWrites

import scala.concurrent.ExecutionContext

class ImageController @Inject()(cc: ImageControllerComponents,
                                imageActionBuilder: ImageActionBuilder,
                                imageService: ImageService,
                                actionBuilder: DefaultActionBuilder,
                                parsers: PlayBodyParsers,
                                messagesApi: MessagesApi,
                                langs: Langs,
                                fileMimeTypes: FileMimeTypes)
                               (implicit ec: ExecutionContext)
  extends ImageBaseController(cc) {

  private val log = Logger(getClass).logger

  private implicit val imageWriter: Writes[Image] = JsonWrites.implicitImageWrites

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
