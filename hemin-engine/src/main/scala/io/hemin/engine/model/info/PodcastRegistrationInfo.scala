package io.hemin.engine.model.info

import java.time.LocalDateTime

final case class PodcastRegistrationInfo(
  timestamp: Option[LocalDateTime] = None,
  complete: Option[Boolean]        = None,
)
