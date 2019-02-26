package hemin.engine.model

/** This is a container class for elements specified in
  * RFC 4287 "The Atom Syndication Format"
  *
  * @see https://tools.ietf.org/html/rfc4287
  * @param contributors Persona information specified in `<atom:contributor>` tags
  * @param links URLs with metadata specified in `<atom:link>` tags
  */
final case class Atom(
  contributors: List[AtomContributor] = Nil,
  links: List[AtomLink]               = Nil,
) extends Patchable[Atom] {

  override def patchLeft(diff: Atom): Atom = Option(diff) match {
    case None       => this
    case Some(that) => Atom(
      contributors = reduceLeft(this.contributors, that.contributors),
      links        = reduceLeft(this.links, that.links),
    )
  }

  override def patchRight(diff: Atom): Atom = Option(diff) match {
    case None       => this
    case Some(that) => Atom(
      contributors = reduceRight(this.contributors, that.contributors),
      links        = reduceRight(this.links, that.links),
    )
  }

}
