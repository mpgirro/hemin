package io.hemin.engine.model

final case class PodcastRegistration(
  timestamp: Option[Long]   = None,
  complete: Option[Boolean] = None,
) extends Patchable[PodcastRegistration] {

  override def patchLeft(diff: PodcastRegistration): PodcastRegistration = Option(diff) match {
    case None       => this
    case Some(that) => PodcastRegistration(
      timestamp = reduceLeft(this.timestamp, that.timestamp),
      complete  = reduceLeft(this.complete, that.complete),
    )
  }

  override def patchRight(diff: PodcastRegistration): PodcastRegistration = Option(diff) match {
    case None       => this
    case Some(that) => PodcastRegistration(
      timestamp = reduceRight(this.timestamp, that.timestamp),
      complete  = reduceRight(this.complete, that.complete),
    )
  }
}
