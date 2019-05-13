package io.hemin.api.v1.rest

import io.hemin.api.v1.rest.base.StatsBaseController
import io.hemin.api.v1.rest.component.StatsControllerComponents
import io.hemin.api.v1.service.StatsService
import io.swagger.annotations.Api
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}

@Api("Statistic")
class StatsController @Inject() (cc: StatsControllerComponents,
                                 statsService: StatsService)
  extends StatsBaseController(cc) {

  private val log = Logger(getClass).logger

  def database: Action[AnyContent] = StatsAction.async {
    implicit request =>
      log.trace(s"GET database stats")
      statsService
        .databaseStats
        .map(s => Ok(Json.toJson(s)))
    }

}
