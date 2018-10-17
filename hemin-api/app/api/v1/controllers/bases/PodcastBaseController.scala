package api.v1.controllers.bases

import api.v1.actions.PodcastActionBuilder
import api.v1.controllers.components.PodcastControllerComponents
import api.v1.services.PodcastService
import api.v1.utils.{ArrayWrapper, JsonWrites, RequestMarkerContext}
import io.hemin.engine.domain.{Episode, Feed, Podcast}
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

  protected implicit val podcastWriter: Writes[Podcast] = JsonWrites.implicitPodcastWrites
  protected implicit val episodeWriter: Writes[Episode] = JsonWrites.implicitEpisodeWrites
  protected implicit val feedWriter: Writes[Feed] = JsonWrites.implicitFeedWrites
  protected implicit val podcastArrayWriter: Writes[ArrayWrapper[Podcast]] = JsonWrites.implicitArrayWrites[Podcast]
  protected implicit val episodeArrayWriter: Writes[ArrayWrapper[Episode]] = JsonWrites.implicitArrayWrites[Episode]
  protected implicit val feedArrayWriter: Writes[ArrayWrapper[Feed]] = JsonWrites.implicitArrayWrites[Feed]

  def PodcastAction: PodcastActionBuilder = cc.actionBuilder

  def podcastService: PodcastService = cc.service
}
