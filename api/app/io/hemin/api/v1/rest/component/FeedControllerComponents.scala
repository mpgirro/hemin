package io.hemin.api.v1.rest.component

import io.hemin.api.v1.action.FeedActionBuilder
import io.hemin.api.v1.service.FeedService
import io.hemin.api.v1.util.ApiV1ExecutionContext
import javax.inject.Inject
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ControllerComponents, PlayBodyParsers}

/**
  * Packages up the component dependencies for the Feed controller.
  *
  * This is a good way to minimize the surface area exposed to the controller, so the
  * controller only has to have one thing injected.
  */
case class FeedControllerComponents @Inject()(actionBuilder: FeedActionBuilder,
                                              service: FeedService,
                                              parsers: PlayBodyParsers,
                                              messagesApi: MessagesApi,
                                              langs: Langs,
                                              fileMimeTypes: FileMimeTypes,
                                              executionContext: ApiV1ExecutionContext)
  extends ControllerComponents
