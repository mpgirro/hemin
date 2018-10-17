package api.v1.controllers

import javax.inject.Inject
import play.api.Logger
import play.api.mvc._

/**
  * A very small controller that renders a home page.
  */
class HomeController @Inject()(cc: ControllerComponents)
  extends AbstractController(cc) {

  private val log = Logger(getClass).logger

  def index = Action { implicit request =>
    Ok(api.v1.views.html.index())
  }

}
