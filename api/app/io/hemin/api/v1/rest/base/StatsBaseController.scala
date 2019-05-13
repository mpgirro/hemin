package io.hemin.api.v1.rest.base

import io.hemin.api.v1.action.StatsActionBuilder
import io.hemin.api.v1.rest.component.StatsControllerComponents
import io.hemin.api.v1.service.StatsService
import io.hemin.api.v1.util.{JsonWrites, RequestMarkerContext}
import io.hemin.engine.model.DatabaseStats
import javax.inject.Inject
import play.api.libs.json.Writes
import play.api.mvc.{BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

/**
  * Exposes actions and handler to the StatsController by wiring the injected state into the base class.
  */
class StatsBaseController @Inject() (cc: StatsControllerComponents)
  extends BaseController with RequestMarkerContext {

  override protected def controllerComponents: ControllerComponents = cc

  protected implicit val executionContext: ExecutionContext = cc.executionContext

  protected implicit val databaseStatsWriter: Writes[DatabaseStats] = JsonWrites.databaseStatsWrites

  def StatsAction: StatsActionBuilder = cc.actionBuilder

  def statsService: StatsService = cc.service
}
