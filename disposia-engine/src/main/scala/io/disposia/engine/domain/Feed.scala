package io.disposia.engine.domain

import java.time.LocalDateTime

import io.disposia.engine.util.mapper.reduce


case class Feed(
  id: Option[String]                           = None,
  podcastId: Option[String]                    = None,
  url: Option[String]                          = None,
  lastChecked: Option[LocalDateTime]           = None,
  lastStatus: Option[FeedStatus]               = None,
  registrationTimestamp: Option[LocalDateTime] = None
) extends Patchable[Feed] {

  def patch(diff: Feed): Feed = Option(diff) match {
    case None => this
    case Some(x) =>
      Feed(
        id                    = reduce(this.id, x.id),
        podcastId             = reduce(this.podcastId, x.podcastId),
        url                   = reduce(this.url, x.url),
        lastChecked           = reduce(this.lastChecked, x.lastChecked),
        lastStatus            = reduce(this.lastStatus, x.lastStatus),
        registrationTimestamp = reduce(this.registrationTimestamp, x.registrationTimestamp),
      )
  }

}
