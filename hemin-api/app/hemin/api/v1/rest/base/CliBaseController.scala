package hemin.api.v1.rest.base

import hemin.api.v1.action.CliActionBuilder
import hemin.api.v1.rest.component.CliControllerComponents
import hemin.api.v1.service.CliService
import hemin.api.v1.util.RequestMarkerContext
import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * Exposes actions and handler to the CliController by wiring the injected state into the base class.
  */
class CliBaseController @Inject()(cc: CliControllerComponents)
  extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = cc

  protected implicit val executionContext: ExecutionContext = cc.executionContext

  def CliAction: CliActionBuilder = cc.actionBuilder

  def cliService: CliService = cc.service

}
