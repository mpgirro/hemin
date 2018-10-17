package api.v1.controllers.bases

import api.v1.actions.SearchActionBuilder
import api.v1.controllers.components.SearchControllerComponents
import api.v1.services.SearchService
import api.v1.utils.RequestMarkerContext
import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */
class SearchBaseController @Inject() (pcc: SearchControllerComponents)
    extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = pcc

  def SearchAction: SearchActionBuilder = pcc.searchActionBuilder

  def searchResourceHandler: SearchService = pcc.searchResourceHandler
}
