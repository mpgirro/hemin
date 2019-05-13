package io.hemin.api.v1.service

import javax.inject.Inject
import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}


/**
  * Controls access to the backend data
  */
class CategoryService @Inject()(engineService: EngineService)
                               (implicit ec: ExecutionContext) {

  private val engine = engineService.engine

  def distinct(implicit mc: MarkerContext): Future[Set[String]] =
    engine.getDistinctCategories

}
