package hemin.api.v1.service

import com.typesafe.config.{Config, ConfigFactory}
import hemin.engine.HeminEngine
import javax.inject._
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future
import scala.util.{Failure, Success}

@Singleton
class EngineService @Inject() (lifecycle: ApplicationLifecycle) {

  private val log = Logger(getClass).logger

  private val config: Config = ConfigFactory.load(System.getProperty("config.resource", "application.conf"))

  val engine: HeminEngine = HeminEngine.boot(config) match {
    case Success(e)  => e
    case Failure(ex) =>
      log.error(s"Terminating due failed Engine initialization; reason : ${ex.getMessage}")
      ex.printStackTrace()
      System.exit(-1)
      null // TODO can I return a better result value (just to please the compiler?)
  }

  lifecycle.addStopHook { () =>
    Future.successful(engine.shutdown())
  }

}
