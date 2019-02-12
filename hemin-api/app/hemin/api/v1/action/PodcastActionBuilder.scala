package hemin.api.v1.action

import hemin.api.v1.request.PodcastRequest
import hemin.api.v1.util.RequestMarkerContext
import javax.inject.Inject
import play.api.http.HttpVerbs
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.{Logger, MarkerContext}

import scala.concurrent.{ExecutionContext, Future}

/**
  * The action builder for Podcast requests.
  *
  * This is the place to put logging, metrics, to augment
  * the request with contextual data, and manipulate the
  * result.
  */
class PodcastActionBuilder @Inject()(messagesApi: MessagesApi,
                                     playBodyParsers: PlayBodyParsers)
                                    (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[PodcastRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {

  override val parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  type PodcastRequestBlock[A] = PodcastRequest[A] => Future[Result]

  private val log = Logger(this.getClass)

  override def invokeBlock[A](request: Request[A],
                              block: PodcastRequestBlock[A]): Future[Result] = {
    // Convert to marker context and use request in block
    implicit val markerContext: MarkerContext = requestHeaderToMarkerContext(request)
    val future = block(new PodcastRequest(request, messagesApi))

    future.map { result =>
      request.method match {
        case GET | HEAD => result.withHeaders("Cache-Control" -> s"max-age: 100")
        case other      => result
      }
    }
  }
}
