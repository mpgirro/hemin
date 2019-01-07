package api.v1.controllers.components

import api.v1.actions.ImageActionBuilder
import api.v1.utils.concurrent.ImageExecutionContext
import api.v1.services.ImageService
import javax.inject.Inject
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ControllerComponents, PlayBodyParsers}

/**
  * Packages up the component dependencies for the Image controller.
  *
  * This is a good way to minimize the surface area exposed to the controller, so the
  * controller only has to have one thing injected.
  */
case class ImageControllerComponents @Inject()(actionBuilder: ImageActionBuilder,
                                               service: ImageService,
                                               parsers: PlayBodyParsers,
                                               messagesApi: MessagesApi,
                                               langs: Langs,
                                               fileMimeTypes: FileMimeTypes,
                                               executionContext: ImageExecutionContext)
  extends ControllerComponents
