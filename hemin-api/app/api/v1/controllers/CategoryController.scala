package api.v1.controllers

import api.v1.controllers.bases.CategoryBaseController
import api.v1.controllers.components.CategoryControllerComponents
import api.v1.services.CategoryService
import io.swagger.annotations.Api
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}

@Api("Category")
class CategoryController @Inject() (cc: CategoryControllerComponents,
                                    categoryService: CategoryService)
  extends CategoryBaseController(cc) {

  private val log = Logger(getClass).logger

  def distinct: Action[AnyContent] =
    CategoryAction.async { implicit request =>
      log.trace(s"GET distinct categories")
      categoryService
        .distinct
        .map(s => Ok(Json.toJson(s)))
    }

}
