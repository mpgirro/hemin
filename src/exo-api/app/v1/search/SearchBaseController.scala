package v1.search

import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}
import util.RequestMarkerContext

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */
class SearchBaseController @Inject() (pcc: SearchControllerComponents)
    extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = pcc

  def SearchAction: SearchActionBuilder = pcc.searchActionBuilder

  def searchResourceHandler: SearchService = pcc.searchResourceHandler
}
