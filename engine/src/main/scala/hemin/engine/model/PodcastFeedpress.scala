package hemin.engine.model

object PodcastFeedpress {
  val empty: PodcastFeedpress = PodcastFeedpress()
}

final case class PodcastFeedpress(
  newsletterId: Option[String] = None,
  locale: Option[String]       = None,
  podcastId: Option[String]    = None,
  cssFile: Option[String]      = None,
) extends Patchable[PodcastFeedpress] {

  override def patchLeft(diff: PodcastFeedpress): PodcastFeedpress = Option(diff) match {
    case None       => this
    case Some(that) => PodcastFeedpress(
      newsletterId = reduceLeft(this.newsletterId, that.newsletterId),
      locale       = reduceLeft(this.locale, that.locale),
      podcastId    = reduceLeft(this.podcastId, that.podcastId),
      cssFile      = reduceLeft(this.cssFile, that.cssFile),
    )
  }

  override def patchRight(diff: PodcastFeedpress): PodcastFeedpress = Option(diff) match {
    case None       => this
    case Some(that) => PodcastFeedpress(
      newsletterId = reduceRight(this.newsletterId, that.newsletterId),
      locale       = reduceRight(this.locale, that.locale),
      podcastId    = reduceRight(this.podcastId, that.podcastId),
      cssFile      = reduceRight(this.cssFile, that.cssFile),
    )
  }
}
