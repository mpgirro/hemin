package services

import exo.engine.ExoEngine
import exo.engine.domain.dto.ResultWrapper

import scala.concurrent.Future

@Singleton
class EngineService {

    val engine: ExoEngine = new ExoEngine()
    engine.start()

}
