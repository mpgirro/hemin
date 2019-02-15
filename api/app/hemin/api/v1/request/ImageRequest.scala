package hemin.api.v1.request

import play.api.i18n.MessagesApi
import play.api.mvc.{MessagesRequestHeader, PreferredMessagesProvider, Request, WrappedRequest}


/**
  * A wrapped request for Image resources.
  *
  * This is commonly used to hold request-specific information like
  * security credentials, and useful shortcut methods.
  */
trait ImageRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider
class ImageRequest[A](request: Request[A], val messagesApi: MessagesApi) extends WrappedRequest(request) with ImageRequestHeader
