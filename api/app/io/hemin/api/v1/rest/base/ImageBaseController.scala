package io.hemin.api.v1.rest.base

import io.hemin.api.v1.action.ImageActionBuilder
import io.hemin.api.v1.rest.component.ImageControllerComponents
import io.hemin.api.v1.service.ImageService
import io.hemin.api.v1.util.{JsonWrites, RequestMarkerContext}
import io.hemin.engine.model.Image
import javax.inject.Inject
import play.api.libs.json.Writes
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * Exposes actions and handler to the ImageController by wiring the injected state into the base class.
  */
class ImageBaseController @Inject()(cc: ImageControllerComponents)
  extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = cc

  protected implicit val executionContext: ExecutionContext = cc.executionContext

  protected implicit val imageWriter: Writes[Image] = JsonWrites.imageWrites

  def ImageAction: ImageActionBuilder = cc.actionBuilder

  def imageService: ImageService = cc.service
}
