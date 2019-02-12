package hemin.api.v1.service

import io.hemin.engine.model._
import javax.inject.Inject
import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}

/**
  * Controls access to the backend data
  */
class FeedService @Inject()(engineService: EngineService)
                           (implicit ec: ExecutionContext) {

  private val engine = engineService.engine

  def find(id: String)(implicit mc: MarkerContext): Future[Option[Feed]] = engine.findFeed(id)

  def propose(url: String)(implicit mc: MarkerContext): Unit = engine.propose(url)

}
