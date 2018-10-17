package api.v1.controllers

import api.v1.actions.EpisodeActionBuilder
import api.v1.concurrent.EpisodeExecutionContext
import api.v1.services.EpisodeService
import javax.inject.Inject
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ControllerComponents, DefaultActionBuilder, PlayBodyParsers}

/**
  * Packages up the component dependencies for the post controller.
  *
  * This is a good way to minimize the surface area exposed to the controller, so the
  * controller only has to have one thing injected.
  */
case class EpisodeControllerComponents @Inject()(episodeActionBuilder: EpisodeActionBuilder,
                                                 episodeResourceHandler: EpisodeService,
                                                 actionBuilder: DefaultActionBuilder,
                                                 parsers: PlayBodyParsers,
                                                 messagesApi: MessagesApi,
                                                 langs: Langs,
                                                 fileMimeTypes: FileMimeTypes,
                                                 executionContext: EpisodeExecutionContext)
  extends ControllerComponents
