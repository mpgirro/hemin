package hemin.api.v1.service

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class CliService @Inject()(engineService: EngineService)
                          (implicit ec: ExecutionContext) {

  private val engine = engineService.engine

  def eval(cmd: String): Future[String] = engine.cli(cmd)

}
