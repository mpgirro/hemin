package api.v1.controllers

import api.v1.actions.PodcastActionBuilder
import api.v1.services.PodcastService
import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}
import api.v1.utils.RequestMarkerContext

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */
class PodcastBaseController @Inject()(pcc: PodcastControllerComponents)
    extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = pcc

  def PodcastAction: PodcastActionBuilder = pcc.podcastActionBuilder

  def podcastResourceHandler: PodcastService = pcc.podcastResourceHandler
}
