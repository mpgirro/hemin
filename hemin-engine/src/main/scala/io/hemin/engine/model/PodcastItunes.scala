package io.hemin.engine.model

final case class PodcastItunes(
  summary: Option[String]     = None,
  author: Option[String]      = None,
  keywords: List[String]      = Nil,
  categories: List[String]    = Nil,
  explicit: Option[Boolean]   = None,
  block: Option[Boolean]      = None,
  podcastType: Option[String] = None,
  ownerName: Option[String]   = None,
  ownerEmail: Option[String]  = None,
) extends Patchable[PodcastItunes] {

  override def patchLeft(diff: PodcastItunes): PodcastItunes = Option(diff) match {
    case None       => this
    case Some(that) => PodcastItunes(
      summary     = reduceLeft(this.summary, that.summary),
      author      = reduceLeft(this.author, that.author),
      keywords    = reduceLeft(this.keywords, that.keywords),
      categories  = reduceLeft(this.categories, that.categories),
      explicit    = reduceLeft(this.explicit, that.explicit),
      block       = reduceLeft(this.block, that.block),
      podcastType = reduceLeft(this.podcastType, that.podcastType),
      ownerName   = reduceLeft(this.ownerName, that.ownerName),
      ownerEmail  = reduceLeft(this.ownerEmail, that.ownerEmail),
    )
  }

  override def patchRight(diff: PodcastItunes): PodcastItunes = Option(diff) match {
    case None       => this
    case Some(that) => PodcastItunes(
      summary     = reduceRight(this.summary, that.summary),
      author      = reduceRight(this.author, that.author),
      keywords    = reduceRight(this.keywords, that.keywords),
      categories  = reduceRight(this.categories, that.categories),
      explicit    = reduceRight(this.explicit, that.explicit),
      block       = reduceRight(this.block, that.block),
      podcastType = reduceRight(this.podcastType, that.podcastType),
      ownerName   = reduceRight(this.ownerName, that.ownerName),
      ownerEmail  = reduceRight(this.ownerEmail, that.ownerEmail),
    )
  }
}
