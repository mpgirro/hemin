package io.hemin.engine.model

import java.time.LocalDateTime

/**
  * @author max
  */
final case class EpisodeRegistration(
  timestamp: Option[LocalDateTime] = None,
)
