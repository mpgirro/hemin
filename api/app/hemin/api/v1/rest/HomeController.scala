package hemin.api.v1.rest

import io.swagger.annotations.Api
import javax.inject.Inject
import play.api.Logger
import play.api.mvc._

/**
  * A very small controller that renders a home page.
  */
@Api("Home")
class HomeController @Inject() (cc: ControllerComponents)
  extends AbstractController(cc) {

  private val log = Logger(getClass).logger

  def index = Action { implicit request =>
    Ok(hemin.api.v1.view.html.index())
  }

}
