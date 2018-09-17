package controllers

import javax.inject.Inject
import play.api.Logger
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import services.SearchService
import v1.post.RequestMarkerContext

import scala.concurrent.{ExecutionContext, Future}


/**
  * @author max
  */
class SearchController @Inject() (cc: ControllerComponents,
                                  postActionBuilder: PostActionBuilder,
                                  postResourceHandler: PostResourceHandler,
                                  actionBuilder: DefaultActionBuilder,
                                  parsers: PlayBodyParsers,
                                  messagesApi: MessagesApi,
                                  langs: Langs,
                                  fileMimeTypes: FileMimeTypes,
                                  searchService: SearchService)
  extends BaseController with RequestMarkerContext {

  private val log = Logger(getClass).logger

  def search(q: String, p: Int, s: Int) = Action {
    implicit request =>

      Ok(views.html.index()) // TODO
  }

  def show(id: String): Action[AnyContent] = PostAction.async { implicit request =>
    logger.trace(s"show: id = $id")
    postResourceHandler.lookup(id).map { post =>
      Ok(Json.toJson(post))
    }
  }
}
