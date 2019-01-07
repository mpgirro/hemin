package api.v1.controllers.components

import api.v1.actions.StatsActionBuilder
import api.v1.services.StatsService
import api.v1.utils.concurrent.StatsExecutionContext
import javax.inject.Inject
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ControllerComponents, PlayBodyParsers}

/**
  * Packages up the component dependencies for the Stats controller.
  *
  * This is a good way to minimize the surface area exposed to the controller, so the
  * controller only has to have one thing injected.
  */
case class StatsControllerComponents @Inject()(actionBuilder: StatsActionBuilder,
                                               service: StatsService,
                                               parsers: PlayBodyParsers,
                                               messagesApi: MessagesApi,
                                               langs: Langs,
                                               fileMimeTypes: FileMimeTypes,
                                               executionContext: StatsExecutionContext)
  extends ControllerComponents
