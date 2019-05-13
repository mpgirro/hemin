package io.hemin.api.v1.request

import play.api.i18n.MessagesApi
import play.api.mvc.{MessagesRequestHeader, PreferredMessagesProvider, Request, WrappedRequest}

/**
  * A wrapped request for CLI resources.
  *
  * This is commonly used to hold request-specific information like
  * security credentials, and useful shortcut methods.
  */
trait CliRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider
class CliRequest[A](request: Request[A], val messagesApi: MessagesApi) extends WrappedRequest(request) with CliRequestHeader
