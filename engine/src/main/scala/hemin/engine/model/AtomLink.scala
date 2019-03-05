package hemin.engine.model

import com.rometools.rome.feed.atom.Link
import com.rometools.rome.feed.synd.SyndLink

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

  /** Instantiates an [[hemin.engine.model.AtomLink]] from a ROME Link object */
  def fromRome(link: Link): AtomLink = AtomLink(
    title        = Option(link.getTitle),
    rel          = Option(link.getRel),
    typ          = Option(link.getType),
    length       = Option(link.getLength),
    href         = Option(link.getHref),
    hrefLang     = Option(link.getHreflang),
    hrefResolved = Option(link.getHrefResolved),
  )

  /** Instantiates an [[hemin.engine.model.AtomLink]] from a ROME SyndLink object */
  def fromRome(link: SyndLink): AtomLink = AtomLink(
    title        = Option(link.getTitle),
    rel          = Option(link.getRel),
    typ          = Option(link.getType),
    length       = Option(link.getLength),
    href         = Option(link.getHref),
    hrefLang     = Option(link.getHreflang),
    hrefResolved = None,
  )

}

case class AtomLink(
  title: Option[String]        = None,
  href: Option[String]         = None,
  hrefResolved: Option[String] = None,
  hrefLang: Option[String]     = None,
  rel: Option[String]          = None,
  typ: Option[String]          = None,
  length: Option[Long]         = None,
)


