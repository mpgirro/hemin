package io.hemin.api.v1.rest.base

import io.hemin.api.v1.action.CategoryActionBuilder
import io.hemin.api.v1.rest.component.CategoryControllerComponents
import io.hemin.api.v1.service.CategoryService
import io.hemin.api.v1.util.RequestMarkerContext
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
