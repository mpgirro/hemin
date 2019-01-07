package api.v1.requests

import play.api.i18n.MessagesApi
import play.api.mvc.{MessagesRequestHeader, PreferredMessagesProvider, Request, WrappedRequest}

/**
  * A wrapped request for Episode resources.
  *
  * This is commonly used to hold request-specific information like
  * security credentials, and useful shortcut methods.
  */
trait EpisodeRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider
class EpisodeRequest[A](request: Request[A], val messagesApi: MessagesApi) extends WrappedRequest(request) with EpisodeRequestHeader
