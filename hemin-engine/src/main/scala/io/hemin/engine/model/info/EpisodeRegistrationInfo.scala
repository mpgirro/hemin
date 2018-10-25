package io.hemin.engine.model.info

import java.time.LocalDateTime

final case class EpisodeRegistrationInfo(
  timestamp: Option[LocalDateTime] = None,
)
