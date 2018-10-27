package api.v1.controllers.bases

import api.v1.actions.CliActionBuilder
import api.v1.controllers.components.CliControllerComponents
import api.v1.services.CliService
import api.v1.utils.RequestMarkerContext
import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

class CliBaseController @Inject()(cc: CliControllerComponents)
  extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = cc

  protected implicit val executionContext: ExecutionContext = cc.executionContext

  def CliAction: CliActionBuilder = cc.actionBuilder

  def cliService: CliService = cc.service

}
