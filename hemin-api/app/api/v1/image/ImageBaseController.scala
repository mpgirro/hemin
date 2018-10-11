package api.v1.image

import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}
import util.RequestMarkerContext

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */
class ImageBaseController @Inject()(pcc: ImageControllerComponents)
  extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = pcc

  def ImageAction: ImageActionBuilder = pcc.imageActionBuilder

  def imageResourceHandler: ImageService = pcc.imageService
}
