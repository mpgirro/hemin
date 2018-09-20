package controllers

import javax.inject.Inject
import play.api.mvc._
import services.EngineService

/**
  * A very small controller that renders a home page.
  */
class HomeController @Inject()(cc: ControllerComponents, engineService: EngineService) extends AbstractController(cc) {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

    def search(q: String, p: Int, s: Int) = Action {
        implicit request =>

            engineService.search(q, p, s)
            Ok(views.html.index()) // TODO
    }
}
