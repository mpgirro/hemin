package io.disposia.engine.newdomain.podcast

import java.time.LocalDateTime

/**
  * @author max
  */
case class PodcastRegistrationInfo(
                                     timestamp: Option[LocalDateTime] = None,
                                     complete: Option[Boolean] = None
                                   )
