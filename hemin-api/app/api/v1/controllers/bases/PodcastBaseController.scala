package api.v1.controllers.bases

import api.v1.actions.PodcastActionBuilder
import api.v1.controllers.components.PodcastControllerComponents
import api.v1.services.PodcastService
import api.v1.utils.{ArrayWrapper, JsonWrites, RequestMarkerContext}
import io.hemin.engine.model.{Episode, Feed, Image, Podcast}
import javax.inject.Inject
import play.api.libs.json.Writes
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */
class PodcastBaseController @Inject()(cc: PodcastControllerComponents)
    extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = cc

  protected implicit val executionContext: ExecutionContext = cc.executionContext

  protected implicit val podcastWriter: Writes[Podcast] = JsonWrites.podcastWrites
  protected implicit val episodeWriter: Writes[Episode] = JsonWrites.episodeWrites
  protected implicit val feedWriter: Writes[Feed] = JsonWrites.feedWrites
  protected implicit val podcastArrayWriter: Writes[ArrayWrapper[Podcast]] = JsonWrites.arrayWrites[Podcast]
  protected implicit val episodeArrayWriter: Writes[ArrayWrapper[Episode]] = JsonWrites.arrayWrites[Episode]
  protected implicit val feedArrayWriter: Writes[ArrayWrapper[Feed]] = JsonWrites.arrayWrites[Feed]
  protected implicit val imageWriter: Writes[Image] = JsonWrites.imageWrites

  def PodcastAction: PodcastActionBuilder = cc.actionBuilder

  def podcastService: PodcastService = cc.service
}
