package hemin.api.v1.rest.base

import hemin.api.v1.action.SearchActionBuilder
import hemin.api.v1.rest.component.SearchControllerComponents
import hemin.api.v1.service.SearchService
import hemin.api.v1.util.{JsonWrites, RequestMarkerContext}
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
