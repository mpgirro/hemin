package controllers

import javax.inject.Inject
import play.api.Logger
import play.api.mvc._
import services.EngineService

/**
  * A very small controller that renders a home page.
  */
class HomeController @Inject()(cc: ControllerComponents,
                               engineService: EngineService)
  extends AbstractController(cc) {

  private val log = Logger(getClass).logger

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

}