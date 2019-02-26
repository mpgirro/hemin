package hemin.engine.model

final case class EpisodeAtom (
  override val contributors: List[AtomContributor] = Nil,
  override val links: List[AtomLink]               = Nil,
) extends Atom {

  override def patchLeft(diff: Atom): EpisodeAtom = Option(diff) match {
    case None       => this
    case Some(that) => EpisodeAtom(
      contributors = reduceLeft(this.contributors, that.contributors),
      links        = reduceLeft(this.links, that.links),
    )
  }

  override def patchRight(diff: Atom): EpisodeAtom = Option(diff) match {
    case None       => this
    case Some(that) => EpisodeAtom(
      contributors = reduceRight(this.contributors, that.contributors),
      links        = reduceRight(this.links, that.links),
    )
  }

}
