package api.v1.controllers

import api.v1.controllers.bases.PodcastBaseController
import api.v1.controllers.components.PodcastControllerComponents
import api.v1.utils.ArrayWrapper
import io.hemin.engine.util.mapper.{EpisodeMapper, PodcastMapper}
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
      log.trace(s"GET all podcasts: p = $p & s = $s")
      podcastService
        .all(p, s)
        .map { ps =>
          Ok(Json.toJson(ArrayWrapper(ps)))
        }
    }

  def allAsTeaser(p: Option[Int], s: Option[Int]): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET all podcasts as teasers: p = $p & s = $s")
      podcastService
        .all(p, s)
        .map(ps => ps.map(PodcastMapper.toTeaser))
        .map(_.flatten)
        .map { ps =>
          Ok(Json.toJson(ArrayWrapper(ps)))
        }
    }

  def episodes(id: String): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET episodes by podcast (reduced to teasers): id = $id")
      podcastService
        .episodes(id)
        .map(_.map(EpisodeMapper.toTeaser))
        .map(_.flatten)
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

  def newest(pageNumber: Option[Int], pageSize: Option[Int]): Action[AnyContent] =
    PodcastAction.async { implicit request =>
      log.trace(s"GET newest podcasts: pageNumber = $pageNumber & pageSize = $pageSize")
      podcastService
        .newest(pageNumber, pageSize)
        .map(ps => Ok(Json.toJson(ArrayWrapper(ps))))
    }
}
