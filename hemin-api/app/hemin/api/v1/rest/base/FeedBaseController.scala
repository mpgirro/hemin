package hemin.api.v1.rest.base

import hemin.api.v1.action.FeedActionBuilder
import hemin.api.v1.rest.component.FeedControllerComponents
import hemin.api.v1.service.FeedService
import hemin.api.v1.util.{ArrayWrapper, JsonWrites, RequestMarkerContext}
import io.hemin.engine.model.Feed
import javax.inject.Inject
import play.api.libs.json.Writes
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * Exposes actions and handler to the FeedController by wiring the injected state into the base class.
  */
class FeedBaseController @Inject()(cc: FeedControllerComponents)
    extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = cc

  protected implicit val executionContext: ExecutionContext = cc.executionContext

  protected implicit val feedWriter: Writes[Feed] = JsonWrites.feedWrites
  protected implicit val feedArrayWriter: Writes[ArrayWrapper[Feed]] = JsonWrites.arrayWrites[Feed]

  def FeedAction: FeedActionBuilder = cc.actionBuilder

  def feedService: FeedService = cc.service
}
