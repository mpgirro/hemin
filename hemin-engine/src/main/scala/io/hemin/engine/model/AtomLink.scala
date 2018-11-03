package io.hemin.engine.model

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
    title = link.getTitle,
    href = link.getHref,
    hrefResolved = link.getHrefResolved,
    hrefLang = link.getHreflang,
    rel = link.getRel,
    typ = link.getType,
    length = link.getLength,
  )

}

case class AtomLink (
  title: String,
  href: String,
  hrefResolved: String,
  hrefLang: String,
  rel: String,
  typ: String,
  length: Long,
)


