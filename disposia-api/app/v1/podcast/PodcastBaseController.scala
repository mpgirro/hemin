package v1.podcast

import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}
import util.RequestMarkerContext

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */
class PodcastBaseController @Inject()(pcc: PodcastControllerComponents)
    extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = pcc

  def PodcastAction: PodcastActionBuilder = pcc.podcastActionBuilder

  def podcastResourceHandler: PodcastService = pcc.podcastResourceHandler
}
