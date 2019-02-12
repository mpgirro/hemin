package hemin.api.v1.service

import io.hemin.engine.model.Image
import javax.inject.Inject
import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}

class ImageService @Inject()(engineService: EngineService)
                            (implicit ec: ExecutionContext) {

  private val engine = engineService.engine

  def find(id: String)(implicit mc: MarkerContext): Future[Option[Image]] = engine.findImage(id)

}
