package hemin.api.v1.rest.component

import hemin.api.v1.action.PodcastActionBuilder
import hemin.api.v1.service.PodcastService
import hemin.api.v1.util.concurrent.PodcastExecutionContext
import javax.inject.Inject
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ControllerComponents, PlayBodyParsers}

/**
  * Packages up the component dependencies for the Podcast controller.
  *
  * This is a good way to minimize the surface area exposed to the controller, so the
  * controller only has to have one thing injected.
  */
case class PodcastControllerComponents @Inject()(actionBuilder: PodcastActionBuilder,
                                                 service: PodcastService,
                                                 parsers: PlayBodyParsers,
                                                 messagesApi: MessagesApi,
                                                 langs: Langs,
                                                 fileMimeTypes: FileMimeTypes,
                                                 executionContext: PodcastExecutionContext)
  extends ControllerComponents
