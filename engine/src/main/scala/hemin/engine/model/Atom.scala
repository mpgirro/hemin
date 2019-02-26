package hemin.engine.model

/** This is a container class for elements specified in
  * RFC 4287 "The Atom Syndication Format"
  *
  * @see https://tools.ietf.org/html/rfc4287
  */
trait Atom extends Patchable[Atom] {

  /** contributors Persona information specified in `<atom:contributor>` tags */
  val contributors: List[AtomContributor]

  /** links URLs with metadata specified in `<atom:link>` tags */
  val links: List[AtomLink]

}
