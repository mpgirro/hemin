package hemin.api.v1.rest.base

import hemin.api.v1.action.PodcastActionBuilder
import hemin.api.v1.rest.component.PodcastControllerComponents
import hemin.api.v1.service.PodcastService
import hemin.api.v1.util.{ArrayWrapper, JsonWrites, RequestMarkerContext}
import hemin.engine.model.{Episode, Feed, Image, Podcast}
import javax.inject.Inject
import play.api.libs.json.Writes
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * Exposes actions and handler to the PodcastController by wiring the injected state into the base class.
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
