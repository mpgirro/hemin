package io.hemin.engine.model

final case class Chapter(
  id: Option[String]        = None, // TODO obsolete and remove?
  episodeId: Option[String] = None, // TODO obsolete and remove?
  start: Option[String]     = None,
  title: Option[String]     = None,
  href: Option[String]      = None,
  image: Option[String]     = None,
) extends Patchable[Chapter] {

  override def patchLeft(diff: Chapter): Chapter = Option(diff) match {
    case None       => this
    case Some(that) => Chapter(
      id        = reduceLeft(this.id, that.id),
      episodeId = reduceLeft(this.episodeId, that.episodeId),
      start     = reduceLeft(this.start, that.start),
      title     = reduceLeft(this.title, that.title),
      href      = reduceLeft(this.href, that.href),
      image     = reduceLeft(this.image, that.image),
    )
  }

  override def patchRight(diff: Chapter): Chapter = Option(diff) match {
    case None       => this
    case Some(that) => Chapter(
      id        = reduceRight(this.id, that.id),
      episodeId = reduceRight(this.episodeId, that.episodeId),
      start     = reduceRight(this.start, that.start),
      title     = reduceRight(this.title, that.title),
      href      = reduceRight(this.href, that.href),
      image     = reduceRight(this.image, that.image),
    )
  }
}
