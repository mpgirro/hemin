package api.v1.controllers.bases

import api.v1.actions.CategoryActionBuilder
import api.v1.controllers.components.CategoryControllerComponents
import api.v1.services.CategoryService
import api.v1.utils.RequestMarkerContext
import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext


/**
  * Exposes actions and handler to the StatsController by wiring the injected state into the base class.
  */
class CategoryBaseController @Inject() (cc: CategoryControllerComponents)
  extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = cc

  protected implicit val executionContext: ExecutionContext = cc.executionContext

  def CategoryAction: CategoryActionBuilder = cc.actionBuilder

  def categoryService: CategoryService = cc.service
}
