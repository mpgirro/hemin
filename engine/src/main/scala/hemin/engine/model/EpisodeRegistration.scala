package hemin.engine.model

object EpisodeRegistration {
  val empty: EpisodeRegistration = EpisodeRegistration()
}

final case class EpisodeRegistration(
  timestamp: Option[Long] = None,
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
