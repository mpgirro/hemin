package api.v1.controllers.bases

import api.v1.actions.FeedActionBuilder
import api.v1.controllers.components.FeedControllerComponents
import api.v1.services.FeedService
import api.v1.utils.{ArrayWrapper, JsonWrites, RequestMarkerContext}
import io.hemin.engine.model.Feed
import javax.inject.Inject
import play.api.libs.json.Writes
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */
class FeedBaseController @Inject()(cc: FeedControllerComponents)
    extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = cc

  protected implicit val executionContext: ExecutionContext = cc.executionContext

  protected implicit val feedWriter: Writes[Feed] = JsonWrites.implicitFeedWrites
  protected implicit val feedArrayWriter: Writes[ArrayWrapper[Feed]] = JsonWrites.implicitArrayWrites[Feed]

  def FeedAction: FeedActionBuilder = cc.actionBuilder

  def feedService: FeedService = cc.service
}
