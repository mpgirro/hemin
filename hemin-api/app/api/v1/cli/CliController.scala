package api.v1.cli

import javax.inject.Inject
import play.api.Logger
import play.api.mvc.{AbstractController, ControllerComponents}


class CliController @Inject()(cc: ControllerComponents,
                              cliService: CliService)
  extends AbstractController(cc) {

  private val log = Logger(getClass).logger

  def eval = Action { implicit request =>
    request.body.asText.map(cmd => {
      log.trace(s"EVAL: $cmd")
      Ok(cliService.eval(cmd))
    }).getOrElse({
      log.warn(s"EVAL: no command given [BadRequest]")
      BadRequest
    })
  }

}
