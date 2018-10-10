package services

import io.hemin.engine.Engine
import javax.inject._
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

@Singleton
class EngineService @Inject() (lifecycle: ApplicationLifecycle) {

  private val log = Logger(getClass).logger

  log.info("Starting engine")
  val engine: Engine = new Engine()
  engine.start()

  lifecycle.addStopHook { () =>
    log.info("Shutting down engine")
    Future.successful(engine.shutdown())
  }

}
