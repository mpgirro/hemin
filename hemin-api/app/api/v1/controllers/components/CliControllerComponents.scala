package api.v1.controllers.components

import api.v1.actions.CliActionBuilder
import api.v1.utils.concurrent.CliExecutionContext
import api.v1.services.CliService
import javax.inject.Inject
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ControllerComponents, PlayBodyParsers}

case class CliControllerComponents @Inject()(actionBuilder: CliActionBuilder,
                                                 service: CliService,
                                                 parsers: PlayBodyParsers,
                                                 messagesApi: MessagesApi,
                                                 langs: Langs,
                                                 fileMimeTypes: FileMimeTypes,
                                                 executionContext: CliExecutionContext)
  extends ControllerComponents
