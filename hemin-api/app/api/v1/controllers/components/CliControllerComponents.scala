package api.v1.controllers.components

import api.v1.actions.CliActionBuilder
import api.v1.utils.concurrent.CliExecutionContext
import api.v1.services.CliService
import javax.inject.Inject
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ControllerComponents, PlayBodyParsers}

/**
  * Packages up the component dependencies for the CLI controller.
  *
  * This is a good way to minimize the surface area exposed to the controller, so the
  * controller only has to have one thing injected.
  */
case class CliControllerComponents @Inject()(actionBuilder: CliActionBuilder,
                                                 service: CliService,
                                                 parsers: PlayBodyParsers,
                                                 messagesApi: MessagesApi,
                                                 langs: Langs,
                                                 fileMimeTypes: FileMimeTypes,
                                                 executionContext: CliExecutionContext)
  extends ControllerComponents
