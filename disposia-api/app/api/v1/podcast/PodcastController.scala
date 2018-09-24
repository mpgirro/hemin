package api.v1.podcast

import io.disposia.engine.domain.dto.{Episode, Feed, Podcast, ResultWrapper}
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
            podcastService.find(id).map { podcast =>
                Ok(Json.toJson(podcast))
            }
        }

    def episodes(id: String): Action[AnyContent] =
        PodcastAction.async { implicit request =>
            log.trace(s"GET episodes by podcast: id = $id")
            podcastService.episodes(id).map { episodes =>
                Ok(Json.toJson(ArrayWrapper(episodes)))
            }
        }

    def feeds(id: String): Action[AnyContent] =
        PodcastAction.async { implicit request =>
            log.trace(s"GET feeds by podcast: id = $id")
            podcastService.feeds(id).map { feeds =>
                Ok(Json.toJson(ArrayWrapper(feeds)))
            }
        }

}
