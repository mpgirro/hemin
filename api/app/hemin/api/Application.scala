package hemin.api

import javax.inject.Inject
import play.api.Logger
import play.api.mvc._


/**
  * A very small controller that renders a home page.
  */
class Application @Inject() (cc: ControllerComponents)
  extends AbstractController(cc) {

  private val log = Logger(getClass).logger

  /*
  def index = Action { implicit request =>
    Ok(hemin.api.v1.view.html.index())
  }
  */

  def redirectDocs = Action { implicit request =>
    Redirect(
      url         = "/docs/",
      queryString = Map.empty,
      status      = play.api.http.Status.OK
    )
  }

}
