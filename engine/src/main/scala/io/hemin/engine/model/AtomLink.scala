package io.hemin.engine.model

import com.rometools.rome.feed.atom.Link
import com.rometools.rome.feed.synd.SyndLink

object AtomLink {

  /** Instantiates an [[io.hemin.engine.model.AtomLink]] from a ROME Link object */
  def fromRome(link: Link): AtomLink = AtomLink(
    title        = Option(link.getTitle),
    rel          = Option(link.getRel),
    typ          = Option(link.getType),
    length       = Option(link.getLength),
    href         = Option(link.getHref),
    hrefLang     = Option(link.getHreflang),
    hrefResolved = Option(link.getHrefResolved),
  )

  /** Instantiates an [[io.hemin.engine.model.AtomLink]] from a ROME SyndLink object */
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

final case class AtomLink(
  title: Option[String]        = None,
  href: Option[String]         = None,
  hrefResolved: Option[String] = None,
  hrefLang: Option[String]     = None,
  rel: Option[String]          = None,
  typ: Option[String]          = None,
  length: Option[Long]         = None,
)


