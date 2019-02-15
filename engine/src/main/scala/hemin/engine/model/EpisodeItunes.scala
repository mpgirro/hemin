package hemin.engine.model

final case class EpisodeItunes(
  duration: Option[String]    = None,
  subtitle: Option[String]    = None,
  author: Option[String]      = None,
  summary: Option[String]     = None,
  season: Option[Int]         = None,
  episode: Option[Int]        = None,
  episodeType: Option[String] = None,
) extends Patchable[EpisodeItunes] {

  override def patchLeft(diff: EpisodeItunes): EpisodeItunes = Option(diff) match {
    case None       => this
    case Some(that) => EpisodeItunes(
      duration    = reduceLeft(this.duration, that.duration),
      subtitle    = reduceLeft(this.subtitle, that.subtitle),
      author      = reduceLeft(this.author, that.author),
      summary     = reduceLeft(this.summary, that.summary),
      season      = reduceLeft(this.season, that.season),
      episode     = reduceLeft(this.episode, that.episode),
      episodeType = reduceLeft(this.episodeType, that.episodeType),
    )
  }

  override def patchRight(diff: EpisodeItunes): EpisodeItunes = Option(diff) match {
    case None       => this
    case Some(that) => EpisodeItunes(
      duration    = reduceRight(this.duration, that.duration),
      subtitle    = reduceRight(this.subtitle, that.subtitle),
      author      = reduceRight(this.author, that.author),
      summary     = reduceRight(this.summary, that.summary),
      season      = reduceRight(this.season, that.season),
      episode     = reduceRight(this.episode, that.episode),
      episodeType = reduceRight(this.episodeType, that.episodeType),
    )
  }
}
