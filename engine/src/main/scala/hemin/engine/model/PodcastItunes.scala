package hemin.engine.model

object PodcastItunes {
  val empty: PodcastItunes = PodcastItunes()
}

final case class PodcastItunes(
  subtitle: Option[String]  = None,
  summary: Option[String]   = None,
  author: Option[String]    = None,
  keywords: List[String]    = Nil,
  categories: List[String]  = Nil,
  explicit: Option[Boolean] = None,
  block: Option[Boolean]    = None,
  typ: Option[String]       = None,
  owner: Option[Person]     = None

) extends Patchable[PodcastItunes] {

  override def patchLeft(diff: PodcastItunes): PodcastItunes = Option(diff) match {
    case None       => this
    case Some(that) => PodcastItunes(
      subtitle   = reduceLeft(this.subtitle, that.subtitle),
      summary    = reduceLeft(this.summary, that.summary),
      author     = reduceLeft(this.author, that.author),
      keywords   = reduceLeft(this.keywords, that.keywords),
      categories = reduceLeft(this.categories, that.categories),
      explicit   = reduceLeft(this.explicit, that.explicit),
      block      = reduceLeft(this.block, that.block),
      typ        = reduceLeft(this.typ, that.typ),
      owner      = reduceLeft(this.owner, that.owner)
    )
  }

  override def patchRight(diff: PodcastItunes): PodcastItunes = Option(diff) match {
    case None       => this
    case Some(that) => PodcastItunes(
      subtitle   = reduceRight(this.subtitle, that.subtitle),
      summary    = reduceRight(this.summary, that.summary),
      author     = reduceRight(this.author, that.author),
      keywords   = reduceRight(this.keywords, that.keywords),
      categories = reduceRight(this.categories, that.categories),
      explicit   = reduceRight(this.explicit, that.explicit),
      block      = reduceRight(this.block, that.block),
      typ        = reduceRight(this.typ, that.typ),
      owner      = reduceRight(this.owner, that.owner)
    )
  }
}
