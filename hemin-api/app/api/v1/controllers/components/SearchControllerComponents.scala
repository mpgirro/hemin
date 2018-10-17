package api.v1.controllers.components

import api.v1.actions.SearchActionBuilder
import api.v1.concurrent.SearchExecutionContext
import api.v1.services.SearchService
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
case class SearchControllerComponents @Inject()(searchActionBuilder: SearchActionBuilder,
                                                searchResourceHandler: SearchService,
                                                actionBuilder: DefaultActionBuilder,
                                                parsers: PlayBodyParsers,
                                                messagesApi: MessagesApi,
                                                langs: Langs,
                                                fileMimeTypes: FileMimeTypes,
                                                executionContext: SearchExecutionContext)
  extends ControllerComponents