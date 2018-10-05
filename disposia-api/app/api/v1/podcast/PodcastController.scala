package api.v1.podcast

import io.disposia.engine.domain._
import javax.inject.Inject
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import util.{ArrayWrapper, JsonWrites}

import scala.concurrent.ExecutionContext


/**
  * @author max
  */
class PodcastController @Inject()(cc: PodcastControllerComponents,
                                  podcastActionBuilder: PodcastActionBuilder,
                                  podcastService: PodcastService,
                                  actionBuilder: DefaultActionBuilder,
                                  parsers: PlayBodyParsers,
                                  messagesApi: MessagesApi,
                                  langs: Langs,
                                  fileMimeTypes: FileMimeTypes)
                                 (implicit ec: ExecutionContext)
  extends PodcastBaseController(cc) {

  private val log = Logger(getClass).logger

  private implicit val podcastWriter: Writes[Podcast] = JsonWrites.implicitPodcastWrites
  private implicit val episodeWriter: Writes[Episode] = JsonWrites.implicitEpisodeWrites
  private implicit val feedWriter: Writes[Feed] = JsonWrites.implicitFeedWrites
  private implicit val podcastArrayWriter: Writes[ArrayWrapper[Podcast]] = JsonWrites.implicitArrayWrites[Podcast]
  private implicit val episodeArrayWriter: Writes[ArrayWrapper[Episode]] = JsonWrites.implicitArrayWrites[Episode]
  private implicit val feedArrayWriter: Writes[ArrayWrapper[Feed]] = JsonWrites.implicitArrayWrites[Feed]

  def find(id: String): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET podcast: id = $id")
      podcastService
        .find(id)
        .map { p =>
          Ok(Json.toJson(p))
        }
    }

  def all(p: Option[Int], s: Option[Int]): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET all podcast: p = $p & s = $s")
      podcastService
        .all(p, s)
        .map { ps =>
          Ok(Json.toJson(ArrayWrapper(ps)))
        }
    }

  def episodes(id: String): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET episodes by podcast: id = $id")
      podcastService
        .episodes(id)
        .map { es =>
          Ok(Json.toJson(ArrayWrapper(es)))
        }
    }

  def feeds(id: String): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET feeds by podcast: id = $id")
      podcastService
        .feeds(id)
        .map { fs =>
          Ok(Json.toJson(ArrayWrapper(fs)))
        }
    }

  def image(id: String): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET image by podcast: id = $id")
      /*
      podcastService.image(id).map { image =>
        Ok(Json.toJson(image))
      }
      */
      throw new UnsupportedOperationException("not yet implemented")
    }

}
