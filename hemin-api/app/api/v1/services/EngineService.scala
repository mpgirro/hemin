package api.v1.services

import com.typesafe.config.ConfigFactory
import io.hemin.engine.Engine
import javax.inject._
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

@Singleton
class EngineService @Inject() (lifecycle: ApplicationLifecycle) {

  private val log = Logger(getClass).logger
  private val config = ConfigFactory.load(System.getProperty("config.resource", "application.conf"))

  log.info("Starting engine")
  val engine: Engine = new Engine(config)

  lifecycle.addStopHook { () =>
    log.info("Shutting down engine")
    Future.successful(engine.shutdown()) // TODO sure that I want future.successful?
  }

}
