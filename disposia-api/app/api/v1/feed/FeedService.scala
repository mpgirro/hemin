package api.v1.feed

import com.google.common.base.Strings.isNullOrEmpty
import com.typesafe.config.ConfigFactory
import io.disposia.engine.domain._
import javax.inject.{Inject, Provider}
import play.api.MarkerContext
import services.EngineService

import scala.concurrent.{ExecutionContext, Future}

// TODO das hier ist ein Service

/**
  * Controls access to the backend data, returning [[io.disposia.engine.domain.Feed]]
  */
class FeedService @Inject()(engineService: EngineService)
                           (implicit ec: ExecutionContext) {

  private val engine = engineService.engine

  def find(id: String)(implicit mc: MarkerContext): Future[Option[Feed]] = engine.findFeed(id)

  def propose(url: String)(implicit mc: MarkerContext): Unit = engine.propose(url)

}
