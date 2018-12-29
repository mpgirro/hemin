package io.hemin.engine.model

import java.time.ZonedDateTime

final case class EpisodeRegistration(
  timestamp: Option[ZonedDateTime] = None,
) extends Patchable[EpisodeRegistration] {

  override def patchLeft(diff: EpisodeRegistration): EpisodeRegistration = Option(diff) match {
    case None       => this
    case Some(that) => EpisodeRegistration(
      timestamp = reduceLeft(this.timestamp, that.timestamp)
    )
  }

  override def patchRight(diff: EpisodeRegistration): EpisodeRegistration = Option(diff) match {
    case None       => this
    case Some(that) => EpisodeRegistration(
      timestamp = reduceRight(this.timestamp, that.timestamp)
    )
  }
}
