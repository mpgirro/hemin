package v1.episode

import javax.inject.Inject
import play.api.mvc.{BaseController, ControllerComponents}
import util.RequestMarkerContext

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */
class EpisodeBaseController @Inject()(pcc: EpisodeControllerComponents)
    extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = pcc

  def EpisodeAction: EpisodeActionBuilder = pcc.episodeActionBuilder

  def episodeResourceHandler: EpisodeService = pcc.episodeResourceHandler
}
