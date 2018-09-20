package services

import exo.engine.domain.dto.Podcast
import javax.inject.Inject

/**
  * @author max
  */
class PodcastService @Inject()(engineService: EngineService) {

    private val engine = engineService.engine


}
