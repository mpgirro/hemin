package hemin.engine.model

import com.rometools.rome.feed.atom.Link

/* these are the kinds of links I've identified so far

atomLink.getRel == "http://podlove.org/deep-link" // TODO this should be a link to the episode website (but is it always though?!)
atomLink.getRel == "payment"
atomLink.getRel == "self"
atomLink.getRel == "alternate"
atomLink.getRel == "first"
atomLink.getRel == "next"
atomLink.getRel == "last"
atomLink.getRel == "hub"
atomLink.getRel == "search"
atomLink.getRel == "via"
atomLink.getRel == "related"
atomLink.getRel == "prev-archive"
*/

object AtomLink {

  /** Instantiates an AtomLink from a ROME Link object */
  def fromRome(link: Link): AtomLink = AtomLink(
    title        = Option(link.getTitle),
    href         = Option(link.getHref),
    hrefResolved = Option(link.getHrefResolved),
    hrefLang     = Option(link.getHreflang),
    rel          = Option(link.getRel),
    typ          = Option(link.getType),
    length       = Option(link.getLength),
  )

}

case class AtomLink (
  title: Option[String],
  href: Option[String],
  hrefResolved: Option[String],
  hrefLang: Option[String],
  rel: Option[String],
  typ: Option[String],
  length: Option[Long],
)


