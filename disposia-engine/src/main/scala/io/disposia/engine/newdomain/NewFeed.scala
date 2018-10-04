package io.disposia.engine.newdomain

import java.time.LocalDateTime

import io.disposia.engine.domain.FeedStatus


case class NewFeed(
  id: Option[String]                           = None,
  podcastId: Option[String]                    = None,
  url: Option[String]                          = None,
  lastChecked: Option[LocalDateTime]           = None,
  lastStatus: Option[FeedStatus]               = None,
  registrationTimestamp: Option[LocalDateTime] = None
)
