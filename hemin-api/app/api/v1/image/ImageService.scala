package api.v1.image

import io.hemin.engine.domain.Image
import javax.inject.Inject
import play.api.MarkerContext
import services.EngineService

import scala.concurrent.{ExecutionContext, Future}

class ImageService @Inject()(engineService: EngineService)
                            (implicit ec: ExecutionContext) {

  private val engine = engineService.engine

  def find(id: String)(implicit mc: MarkerContext): Future[Option[Image]] = engine.findImage(id)

}
