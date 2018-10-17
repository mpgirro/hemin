package api.v1.controllers.bases

import api.v1.actions.ImageActionBuilder
import api.v1.controllers.components.ImageControllerComponents
import api.v1.services.ImageService
import api.v1.utils.RequestMarkerContext
import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */
class ImageBaseController @Inject()(pcc: ImageControllerComponents)
  extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = pcc

  def ImageAction: ImageActionBuilder = pcc.imageActionBuilder

  def imageResourceHandler: ImageService = pcc.imageService
}
