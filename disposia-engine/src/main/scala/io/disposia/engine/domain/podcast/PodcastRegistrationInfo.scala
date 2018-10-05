package io.disposia.engine.domain.podcast

import java.time.LocalDateTime

case class PodcastRegistrationInfo(
                                     timestamp: Option[LocalDateTime] = None,
                                     complete: Option[Boolean] = None
                                   )
