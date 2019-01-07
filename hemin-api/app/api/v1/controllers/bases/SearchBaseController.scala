package api.v1.controllers.bases

import api.v1.actions.SearchActionBuilder
import api.v1.controllers.components.SearchControllerComponents
import api.v1.services.SearchService
import api.v1.utils.{JsonWrites, RequestMarkerContext}
import io.hemin.engine.model.SearchResult
import javax.inject.Inject
import play.api.libs.json.Writes
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * Exposes actions and handler to the SearchController by wiring the injected state into the base class.
  */
class SearchBaseController @Inject() (cc: SearchControllerComponents)
    extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = cc

  protected implicit val executionContext: ExecutionContext = cc.executionContext

  protected implicit val searchWriter: Writes[SearchResult] = JsonWrites.searchResultWrites

  def SearchAction: SearchActionBuilder = cc.actionBuilder

  def searchService: SearchService = cc.service
}
