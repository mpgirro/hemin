package controllers

import javax.inject.Inject
import play.api.mvc.ControllerComponents


/**
  * @author max
  */
class SearchController @Inject() (cc: ControllerComponents
                                  searchService: SearchService) extends AbstractController(cc) {



  def search(q: String, p: Int, s: Int) = Action {
    implicit request =>
      Ok(views.html.index()) // TODO
  }
}