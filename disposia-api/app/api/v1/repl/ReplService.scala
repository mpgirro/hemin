package api.v1.repl

import io.disposia.engine.cnc.ReplProcessor
import javax.inject.Inject
import services.EngineService

import scala.concurrent.ExecutionContext

class ReplService @Inject()(engineService: EngineService)
                           (implicit ec: ExecutionContext) {

  private val engine = engineService.engine
  private val processor = new ReplProcessor(engine.bus, engine.config, ec)

  def eval(cmd: String): String = processor.eval(cmd)

}
