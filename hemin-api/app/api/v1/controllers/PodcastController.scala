package api.v1.controllers

import api.v1.controllers.bases.PodcastBaseController
import api.v1.controllers.components.PodcastControllerComponents
import api.v1.utils.ArrayWrapper
import io.swagger.annotations.Api
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

@Api("Podcast")
class PodcastController @Inject() (cc: PodcastControllerComponents)
  extends PodcastBaseController(cc) {

  private val log = Logger(getClass).logger

  def find(id: String): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET podcast: id = $id")
      podcastService
        .find(id)
        .map {
          case Some(p) => Ok(Json.toJson(p))
          case None    => NotFound
        }
    }

  def all(p: Option[Int], s: Option[Int]): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET all podcast: p = $p & s = $s")
      podcastService
        .all(p, s)
        .map { ps =>
          Ok(Json.toJson(ArrayWrapper(ps)))
        }
    }

  def episodes(id: String): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET episodes by podcast: id = $id")
      podcastService
        .episodes(id)
        .map { es =>
          Ok(Json.toJson(ArrayWrapper(es)))
        }
    }

  def feeds(id: String): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET feeds by podcast: id = $id")
      podcastService
        .feeds(id)
        .map { fs =>
          Ok(Json.toJson(ArrayWrapper(fs)))
        }
    }

}
