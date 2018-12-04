package api.v1.controllers.bases

import api.v1.actions.ImageActionBuilder
import api.v1.controllers.components.ImageControllerComponents
import api.v1.services.ImageService
import api.v1.utils.{JsonWrites, RequestMarkerContext}
import io.hemin.engine.model.Image
import javax.inject.Inject
import play.api.libs.json.Writes
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */
class ImageBaseController @Inject()(cc: ImageControllerComponents)
  extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = cc

  protected implicit val executionContext: ExecutionContext = cc.executionContext

  protected implicit val imageWriter: Writes[Image] = JsonWrites.imageWrites

  def ImageAction: ImageActionBuilder = cc.actionBuilder

  def imageService: ImageService = cc.service
}
