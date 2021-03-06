package io.hemin.engine.model

import reactivemongo.bson.Macros.Annotations.Key

final case class Feed(
  @Key("_id")
  id: Option[String]                  = None,
  primary: Boolean                    = false,
  podcastId: Option[String]           = None,
  url: Option[String]                 = None,
  lastChecked: Option[Long]           = None,
  lastStatus: Option[FeedStatus]      = None,
  registrationTimestamp: Option[Long] = None,
) extends Patchable[Feed] {

  override def patchLeft(diff: Feed): Feed = Option(diff) match {
    case None       => this
    case Some(that) => Feed(
      id                    = reduceLeft(this.id, that.id),
      podcastId             = reduceLeft(this.podcastId, that.podcastId),
      url                   = reduceLeft(this.url, that.url),
      lastChecked           = reduceLeft(this.lastChecked, that.lastChecked),
      lastStatus            = reduceLeft(this.lastStatus, that.lastStatus),
      registrationTimestamp = reduceLeft(this.registrationTimestamp, that.registrationTimestamp),
    )
  }

  override def patchRight(diff: Feed): Feed = Option(diff) match {
    case None       => this
    case Some(that) => Feed(
      id                    = reduceRight(this.id, that.id),
      podcastId             = reduceRight(this.podcastId, that.podcastId),
      url                   = reduceRight(this.url, that.url),
      lastChecked           = reduceRight(this.lastChecked, that.lastChecked),
      lastStatus            = reduceRight(this.lastStatus, that.lastStatus),
      registrationTimestamp = reduceRight(this.registrationTimestamp, that.registrationTimestamp),
    )
  }
}
