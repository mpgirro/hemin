package v1.feed

import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}
import util.RequestMarkerContext

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */
class FeedBaseController @Inject()(pcc: FeedControllerComponents)
    extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = pcc

  def FeedAction: FeedActionBuilder = pcc.feedActionBuilder

  def feedResourceHandler: FeedService = pcc.feedService
}
