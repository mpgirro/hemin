package api.v1.services

import io.hemin.engine.util.cli.CliProcessor
import javax.inject.Inject

import scala.concurrent.ExecutionContext

class CliService @Inject()(engineService: EngineService)
                          (implicit ec: ExecutionContext) {

  private val engine = engineService.engine
  private val processor = new CliProcessor(engine.bus, engine.config, ec)

  def eval(cmd: String): String = processor.eval(cmd)

}