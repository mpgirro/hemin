package api.v1.feed

import io.hemin.engine.domain._
import javax.inject.Inject
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import util.{ArrayWrapper, JsonWrites}

import scala.concurrent.ExecutionContext


class FeedController @Inject()(cc: FeedControllerComponents,
                               feedActionBuilder: FeedActionBuilder,
                               feedService: FeedService,
                               actionBuilder: DefaultActionBuilder,
                               parsers: PlayBodyParsers,
                               messagesApi: MessagesApi,
                               langs: Langs,
                               fileMimeTypes: FileMimeTypes)
                              (implicit ec: ExecutionContext)
  extends FeedBaseController(cc) {

  private val log = Logger(getClass).logger

  private implicit val feedWriter: Writes[Feed] = JsonWrites.implicitFeedWrites
  private implicit val feedArrayWriter: Writes[ArrayWrapper[Feed]] = JsonWrites.implicitArrayWrites[Feed]

  def find(id: String): Action[AnyContent] =
    FeedAction.async { implicit request =>
      log.trace(s"GET feed: id = $id")
      feedService
        .find(id)
        .map { f =>
          Ok(Json.toJson(f))
        }
    }

  def propose = Action { implicit request =>
    request.body.asText.map(url => {
      log.trace(s"propose feed: $url")
      feedService.propose(url)
      Ok
    }).getOrElse({
      log.warn(s"propose feed: No URL in body [BadRequest]")
      BadRequest
    })
  }

}
