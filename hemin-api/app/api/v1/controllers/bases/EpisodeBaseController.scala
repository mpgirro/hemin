package api.v1.controllers.bases

import api.v1.actions.EpisodeActionBuilder
import api.v1.controllers.components.EpisodeControllerComponents
import api.v1.services.EpisodeService
import api.v1.utils.{ArrayWrapper, JsonWrites, RequestMarkerContext}
import io.hemin.engine.domain.{Chapter, Episode}
import javax.inject.Inject
import play.api.libs.json.Writes
import play.api.mvc.{BaseController, ControllerComponents}

/**
  * Exposes actions and handler to the EpisodeController by wiring the injected state into the base class.
  */
class EpisodeBaseController @Inject()(cc: EpisodeControllerComponents)
    extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = cc

  protected implicit val episodeWriter: Writes[Episode] = JsonWrites.implicitEpisodeWrites
  protected implicit val chapterWriter: Writes[Chapter] = JsonWrites.implicitChapterWrites
  protected implicit val episodeArrayWriter: Writes[ArrayWrapper[Episode]] = JsonWrites.implicitArrayWrites[Episode]
  protected implicit val chapterArrayWriter: Writes[ArrayWrapper[Chapter]] = JsonWrites.implicitArrayWrites[Chapter]

  protected def EpisodeAction: EpisodeActionBuilder = cc.actionBuilder

  protected def episodeService: EpisodeService = cc.service

}
