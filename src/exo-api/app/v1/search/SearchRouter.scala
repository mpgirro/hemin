package v1.search

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/**
  * @author max
  */
class SearchRouter @Inject()(controller: SearchController) extends SimpleRouter {
  val prefix = "/v1/search"

  override def routes: Routes = {

      /*
    case GET(p"/") =>
      controller.index

    case POST(p"/") =>
      controller.process

    case GET(p"/$id") =>
      controller.show(id)
      */


    case GET(p"/q=$q&p=$p&s=$s") =>
        controller.search(q,Option(p.toInt),Option(s.toInt))

  }

}
