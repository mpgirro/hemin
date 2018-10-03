package io.disposia.engine.experimental

import java.time.LocalDateTime


case class PodcastRegistrationInfo (
                                     timestamp: Option[LocalDateTime] = None,
                                     complete: Option[Boolean] = None
                                   )
