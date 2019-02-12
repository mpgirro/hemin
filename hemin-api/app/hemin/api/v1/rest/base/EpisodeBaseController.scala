package hemin.api.v1.rest.base

import hemin.api.v1.action.EpisodeActionBuilder
import hemin.api.v1.rest.component.EpisodeControllerComponents
import hemin.api.v1.service.EpisodeService
import hemin.api.v1.util.{ArrayWrapper, JsonWrites, RequestMarkerContext}
import io.hemin.engine.model.{Chapter, Episode, Image}
import javax.inject.Inject
import play.api.libs.json.Writes
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * Exposes actions and handler to the EpisodeController by wiring the injected state into the base class.
  */
class EpisodeBaseController @Inject()(cc: EpisodeControllerComponents)
    extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = cc

  protected implicit val executionContext: ExecutionContext = cc.executionContext

  protected implicit val episodeWriter: Writes[Episode] = JsonWrites.episodeWrites
  protected implicit val chapterWriter: Writes[Chapter] = JsonWrites.chapterWrites
  protected implicit val episodeArrayWriter: Writes[ArrayWrapper[Episode]] = JsonWrites.arrayWrites[Episode]
  protected implicit val chapterArrayWriter: Writes[ArrayWrapper[Chapter]] = JsonWrites.arrayWrites[Chapter]
  protected implicit val imageWriter: Writes[Image] = JsonWrites.imageWrites

  protected def EpisodeAction: EpisodeActionBuilder = cc.actionBuilder

  protected def episodeService: EpisodeService = cc.service

}
