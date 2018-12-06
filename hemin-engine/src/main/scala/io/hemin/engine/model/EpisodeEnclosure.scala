package io.hemin.engine.model

final case class EpisodeEnclosure(
  url: Option[String]  = None,
  length: Option[Long] = None,
  typ: Option[String]  = None,
) extends Patchable[EpisodeEnclosure] {

  override def patchLeft(diff: EpisodeEnclosure): EpisodeEnclosure = Option(diff) match {
    case None => this
    case Some(that) => EpisodeEnclosure(
      url    = reduceLeft(this.url, that.url),
      length = reduceLeft(this.length, that.length),
      typ    = reduceLeft(this.typ, that.typ),
    )
  }

  override def patchRight(diff: EpisodeEnclosure): EpisodeEnclosure = Option(diff) match {
    case None => this
    case Some(that) => EpisodeEnclosure(
      url    = reduceRight(this.url, that.url),
      length = reduceRight(this.length, that.length),
      typ    = reduceRight(this.typ, that.typ),
    )
  }
}
