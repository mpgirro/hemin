package controllers

import javax.inject.Inject
import play.api.Logger
import play.api.mvc._
import services.EngineService

/**
  * A very small controller that renders a home page.
  */
class PodcastController @Inject()(cc: ControllerComponents,
                                  engineService: EngineService)
    extends AbstractController(cc) {

    private val log = Logger(getClass).logger

    def index = Action { implicit request =>
        Ok(views.html.index())
    }

    def getPodcast(id: String) = Action { implicit request =>


        engineService.engine.findPodcast(id)

        request.body.asText.map(url => {
            log.trace(s"propose: $url")
            engineService.engine.propose(url)
            Ok
        }).getOrElse({
            log.warn(s"propose: No URL in body [BadRequest]")
            BadRequest
        })
    }

}
