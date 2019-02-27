package hemin.engine.model

final case class Feedpress(
  newsletterId: Option[String] = None,
  locale: Option[String]       = None,
  podcastId: Option[String]    = None,
  cssFile: Option[String]      = None,
) extends Patchable[Feedpress] {

  override def patchLeft(diff: Feedpress): Feedpress = Option(diff) match {
    case None       => this
    case Some(that) => Feedpress(
      newsletterId = reduceLeft(this.newsletterId, that.newsletterId),
      locale       = reduceLeft(this.locale, that.locale),
      podcastId    = reduceLeft(this.podcastId, that.podcastId),
      cssFile      = reduceLeft(this.cssFile, that.cssFile),
    )
  }

  override def patchRight(diff: Feedpress): Feedpress = Option(diff) match {
    case None       => this
    case Some(that) => Feedpress(
      newsletterId = reduceRight(this.newsletterId, that.newsletterId),
      locale       = reduceRight(this.locale, that.locale),
      podcastId    = reduceRight(this.podcastId, that.podcastId),
      cssFile      = reduceRight(this.cssFile, that.cssFile),
    )
  }

}
