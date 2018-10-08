package api.v1.repl

import javax.inject.Inject
import play.api.Logger
import play.api.mvc.{AbstractController, ControllerComponents}


class ReplController @Inject()(cc: ControllerComponents,
                               replService: ReplService)
  extends AbstractController(cc) {

  private val log = Logger(getClass).logger

  def eval = Action { implicit request =>
    request.body.asText.map(cmd => {
      log.trace(s"EVAL: $cmd")
      Ok(replService.eval(cmd))
    }).getOrElse({
      log.warn(s"EVAL: no command given [BadRequest]")
      BadRequest
    })
  }

}
