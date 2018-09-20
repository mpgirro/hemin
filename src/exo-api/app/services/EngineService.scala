package services

import exo.engine.ExoEngine
import javax.inject._
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

@Singleton
class EngineService @Inject() (lifecycle: ApplicationLifecycle) {

    val engine: ExoEngine = new ExoEngine()
    engine.start()

    lifecycle.addStopHook { () =>
        Future.successful(engine.shutdown())
    }

}
