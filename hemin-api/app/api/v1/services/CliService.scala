package api.v1.services

import javax.inject.Inject

import scala.concurrent.ExecutionContext

class CliService @Inject()(engineService: EngineService)
                          (implicit ec: ExecutionContext) {

  private val engine = engineService.engine
  private val processor = engine.cliProcessor(ec)

  def eval(cmd: String): String = processor.eval(cmd)

}
