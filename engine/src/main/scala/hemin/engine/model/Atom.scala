package hemin.engine.model

/** This is a container class for elements specified in
  * RFC 4287 "The Atom Syndication Format"
  *
  * @see https://tools.ietf.org/html/rfc4287
  * @param links
  */
final case class Atom (
  links: List[AtomLink] = Nil,
) extends Patchable[Atom] {

  override def patchLeft(diff: Atom): Atom = Option(diff) match {
    case None       => this
    case Some(that) => Atom(
      links = reduceLeft(this.links, that.links),
    )
  }

  override def patchRight(diff: Atom): Atom = Option(diff) match {
    case None       => this
    case Some(that) => Atom(
      links = reduceRight(this.links, that.links),
    )
  }

}

