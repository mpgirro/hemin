package io.hemin.api.v1.rest

import io.hemin.api.v1.rest.base.CliBaseController
import io.hemin.api.v1.rest.component.CliControllerComponents
import io.hemin.api.v1.service.CliService
import io.swagger.annotations.Api
import javax.inject.Inject
import play.api.Logger
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.Future

@Api("Cli")
class CliController @Inject() (cc: CliControllerComponents,
                               cliService: CliService)
  extends CliBaseController(cc) {

  private val log = Logger(getClass).logger

  def eval: Action[AnyContent] = CliAction.async {
    implicit request =>
      request.body.asText match {
        case Some(cmd) =>
          log.trace(s"EVAL: $cmd")
          cliService
            .eval(cmd)
            .map(txt => Ok(txt))
        case None =>
          log.warn(s"EVAL: no command given [BadRequest]")
          Future.successful(BadRequest)
      }
    }

}
