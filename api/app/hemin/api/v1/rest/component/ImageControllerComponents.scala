package hemin.api.v1.rest.component

import hemin.api.v1.action.ImageActionBuilder
import hemin.api.v1.service.ImageService
import hemin.api.v1.util.concurrent.ApiV1ExecutionContext
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
                                               executionContext: ApiV1ExecutionContext)
  extends ControllerComponents
