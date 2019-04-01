package hemin.engine.model

object PodcastFyyd {
  val empty: PodcastFyyd = PodcastFyyd()
}

final case class PodcastFyyd(
  verify: Option[String] = None,
) extends Patchable[PodcastFyyd] {

  override def patchLeft(diff: PodcastFyyd): PodcastFyyd = Option(diff) match {
    case None       => this
    case Some(that) => PodcastFyyd(
      verify = reduceLeft(this.verify, that.verify),
    )
  }

  override def patchRight(diff: PodcastFyyd): PodcastFyyd = Option(diff) match {
    case None       => this
    case Some(that) => PodcastFyyd(
      verify = reduceRight(this.verify, that.verify),
    )
  }
}
