package io.hemin.engine.domain.info

import java.time.LocalDateTime

final case class EpisodeRegistrationInfo(
  timestamp: Option[LocalDateTime] = None,
)
