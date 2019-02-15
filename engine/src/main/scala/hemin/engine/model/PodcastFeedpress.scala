package hemin.engine.model

final case class PodcastFeedpress(
  locale: Option[String] = None,
) extends Patchable[PodcastFeedpress] {

  override def patchLeft(diff: PodcastFeedpress): PodcastFeedpress = Option(diff) match {
    case None       => this
    case Some(that) => PodcastFeedpress(
      locale = reduceLeft(this.locale, that.locale),
    )
  }

  override def patchRight(diff: PodcastFeedpress): PodcastFeedpress = Option(diff) match {
    case None       => this
    case Some(that) => PodcastFeedpress(
      locale = reduceRight(this.locale, that.locale),
    )
  }
}
