package hemin.api.v1.service

import hemin.engine.model.DatabaseStats
import javax.inject.Inject
import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}

/**
  * Controls access to the backend data
  */
class StatsService @Inject()(engineService: EngineService)
                             (implicit ec: ExecutionContext) {

  private val engine = engineService.engine

  def databaseStats(implicit mc: MarkerContext): Future[DatabaseStats] =
    engine.getDatabaseStats

}
