package api.v1.controllers.bases

import api.v1.actions.PodcastActionBuilder
import api.v1.controllers.components.PodcastControllerComponents
import api.v1.services.PodcastService
import api.v1.utils.RequestMarkerContext
import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */
class PodcastBaseController @Inject()(pcc: PodcastControllerComponents)
    extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = pcc

  def PodcastAction: PodcastActionBuilder = pcc.podcastActionBuilder

  def podcastResourceHandler: PodcastService = pcc.podcastResourceHandler
}
