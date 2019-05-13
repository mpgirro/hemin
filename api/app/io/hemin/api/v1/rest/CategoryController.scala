package io.hemin.api.v1.rest

import io.hemin.api.v1.rest.base.CategoryBaseController
import io.hemin.api.v1.rest.component.CategoryControllerComponents
import io.hemin.api.v1.service.CategoryService
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

  def distinct: Action[AnyContent] = CategoryAction.async {
    implicit request =>
      log.trace(s"GET distinct categories")
      categoryService
        .distinct
        .map(s => Ok(Json.toJson(s)))
    }

}
