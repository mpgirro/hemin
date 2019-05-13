package io.hemin.api.v1.request

import play.api.i18n.MessagesApi
import play.api.mvc.{MessagesRequestHeader, PreferredMessagesProvider, Request, WrappedRequest}

/**
  * A wrapped request for Category resources.
  *
  * This is commonly used to hold request-specific information like
  * security credentials, and useful shortcut methods.
  */
trait CategoryRequestHeader extends MessagesRequestHeader with PreferredMessagesProvider
class CategoryRequest[A](request: Request[A], val messagesApi: MessagesApi) extends WrappedRequest(request) with CategoryRequestHeader
