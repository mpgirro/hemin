package v1.podcast

import io.disposia.engine.domain.dto.{Podcast, ResultWrapper}
import javax.inject.Inject
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import util.{JsonWrites, RequestMarkerContext}

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

    def find(id: String): Action[AnyContent] =
        PodcastAction.async { implicit request =>
            log.trace(s"find: id = $id")
            podcastService.find(id).map { podcast =>
                Ok(Json.toJson(podcast))
            }
        }

}
