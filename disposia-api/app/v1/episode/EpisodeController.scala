package v1.episode

import io.disposia.engine.domain.dto._
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
class EpisodeController @Inject()(cc: EpisodeControllerComponents,
                                  episodeActionBuilder: EpisodeActionBuilder,
                                  episodeService: EpisodeService,
                                  actionBuilder: DefaultActionBuilder,
                                  parsers: PlayBodyParsers,
                                  messagesApi: MessagesApi,
                                  langs: Langs,
                                  fileMimeTypes: FileMimeTypes)
                                 (implicit ec: ExecutionContext)
    extends EpisodeBaseController(cc) {

    private val log = Logger(getClass).logger

    private implicit val episodeWriter: Writes[Episode] = JsonWrites.implicitEpisodeWrites
    private implicit val chapterWriter: Writes[Chapter] = JsonWrites.implicitChapterWrites
    private implicit val episodeArrayWriter: Writes[ArrayWrapper[Episode]] = JsonWrites.implicitArrayWrites[Episode]
    private implicit val chapterArrayWriter: Writes[ArrayWrapper[Chapter]] = JsonWrites.implicitArrayWrites[Chapter]

    def find(id: String): Action[AnyContent] =
        EpisodeAction.async { implicit request =>
            log.trace(s"GET episode: id = $id")
            episodeService.find(id).map { episode =>
                Ok(Json.toJson(episode))
            }
        }

    def chapters(id: String): Action[AnyContent] =
        EpisodeAction.async { implicit request =>
            log.trace(s"GET chapters by episode: id = $id")
            episodeService.chapters(id).map { chapters =>
                Ok(Json.toJson(ArrayWrapper(chapters)))
            }
        }

}
