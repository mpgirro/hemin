package io.disposia.engine.newdomain

import java.time.LocalDateTime

import io.disposia.engine.domain.FeedStatus
import io.disposia.engine.util.mapper.reduce


case class NewFeed(
  id: Option[String]                           = None,
  podcastId: Option[String]                    = None,
  url: Option[String]                          = None,
  lastChecked: Option[LocalDateTime]           = None,
  lastStatus: Option[FeedStatus]               = None,
  registrationTimestamp: Option[LocalDateTime] = None
) {

  /**
    * Updates the current instance's fields by all non-None fields of the patch
    *
    * @param patch the instance with set fields that need updating
    * @return the updated instance
    */
  def update(patch: NewFeed): NewFeed = {
    Option(patch) match {
      case None => this
      case Some(p) =>
        NewFeed(
          id                    = reduce(this.id, p.id),
          podcastId             = reduce(this.podcastId, p.podcastId),
          url                   = reduce(this.url, p.url),
          lastChecked           = reduce(this.lastChecked, p.lastChecked),
          lastStatus            = reduce(this.lastStatus, p.lastStatus),
          registrationTimestamp = reduce(this.registrationTimestamp, p.registrationTimestamp),
        )
    }
  }

}
