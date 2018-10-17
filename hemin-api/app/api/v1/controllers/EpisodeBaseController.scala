package api.v1.controllers

import api.v1.actions.EpisodeActionBuilder
import api.v1.services.EpisodeService
import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}
import api.v1.utils.RequestMarkerContext

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */
class EpisodeBaseController @Inject()(pcc: EpisodeControllerComponents)
    extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = pcc

  def EpisodeAction: EpisodeActionBuilder = pcc.episodeActionBuilder

  def episodeResourceHandler: EpisodeService = pcc.episodeResourceHandler
}
