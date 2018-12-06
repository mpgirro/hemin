package api.v1.controllers.components

import api.v1.actions.EpisodeActionBuilder
import api.v1.utils.concurrent.EpisodeExecutionContext
import api.v1.services.EpisodeService
import javax.inject.Inject
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ControllerComponents, PlayBodyParsers}

/**
  * Packages up the component dependencies for the Episode controller.
  *
  * This is a good way to minimize the surface area exposed to the controller, so the
  * controller only has to have one thing injected.
  */
case class EpisodeControllerComponents @Inject()(actionBuilder: EpisodeActionBuilder,
                                                 service: EpisodeService,
                                                 parsers: PlayBodyParsers,
                                                 messagesApi: MessagesApi,
                                                 langs: Langs,
                                                 fileMimeTypes: FileMimeTypes,
                                                 executionContext: EpisodeExecutionContext)
  extends ControllerComponents
