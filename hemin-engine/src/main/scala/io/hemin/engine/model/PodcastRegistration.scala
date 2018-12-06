package io.hemin.engine.model

import java.time.LocalDateTime

/**
  * @author max
  */
final case class PodcastRegistration(
  timestamp: Option[LocalDateTime] = None,
  complete: Option[Boolean]        = None,
)
