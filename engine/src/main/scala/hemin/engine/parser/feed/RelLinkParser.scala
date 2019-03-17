package hemin.engine.parser.feed

import hemin.engine.model.AtomLink
import hemin.engine.parser.feed.RelLinkParser._

object RelLinkParser {

  // Described for the Atom Syndication Format (RFC 4287)
  val ALTERNATE: String = "alternate"  // alternate representation of the entry or feed
  val ENCLOSURE: String = "enclosure"  // related resource (media file)
  val RELATED: String   = "related"    // document related to the entry or feed
  val SELF: String      = "self"       // the feed itself
  val VIA: String       = "via"        // source of the information provided in the entry

  // Described for Paged Feeds (RFC 5005)
  val FIRST: String    = "first"       // furthest preceding document in a series of documents
  val LAST: String     = "last"        // furthest following document in a series of documents
  val PREVIOUS: String = "previous"    // immediately preceding document in a series of documents
  val NEXT: String     = "next"        // immediately following document in a series of documents

  // Described for Archived Feeds (RFC 5005)
  val PREV_ARCHIVE: String = "prev-archive"  // immediately preceding archive document
  val NEXT_ARCHIVE: String = "next-archive"  // immediately following archive document
  val CURRENT: String      = "current"       // feed document containing the most recent entries

  // Described for Podlove Deep Link at http://podlove.org/deep-link
  val DEEP_LINK: String = "http://podlove.org/deep-link"

  // unsorted
  val PAYMENT: String = "payment"
  val HUB: String     = "hub"
  val SEARCH: String  = "search"
  val REPLIES: String = "replies"  // comments related to this entry

}

/** The SemanticLinkParser evaluates a list of Atom links by their
  * relation ("rel") attribute and sorts them by their semantic
  * definition.
  *
  * @param links
  */
class RelLinkParser(links: List[AtomLink]) {

  private val pairs: List[(String, String)] = links
    .map(l => (l.rel, l.href))
    .map {
      case (Some(rel), Some(href)) => (rel,href)
    }

  val alternate: Set[String] = matchMore(ALTERNATE)

  val enclosure: Set[String] = matchMore(ENCLOSURE)

  val related: Set[String] = matchMore(RELATED)

  val self: Option[String] = matchOne(SELF)

  val via: Set[String] = matchMore(VIA)

  val pagedFeeds: Set[String] = pairs
    .filter(p => isPagedFeed(p._1))
    .map(p => p._2)
    .toSet

  val pagedFeedFirst: Option[String] = matchOne(FIRST)

  val pagedFeedLast: Option[String] = matchOne(LAST)

  val pagedFeedPrevious: Option[String] = matchOne(PREVIOUS)

  val pagedFeedNext: Option[String] = matchOne(NEXT)

  val archivedFeeds: Set[String] = pairs
    .filter(p => isArchivedFeed(p._1))
    .map(p => p._2)
    .toSet

  val archivedFeedPrev: Option[String] = matchOne(PREV_ARCHIVE)

  val archivedFeedNext: Option[String] = matchOne(NEXT_ARCHIVE)

  val archivedFeedCurrent: Option[String] = matchOne(DEEP_LINK)

  val deepLink: Option[String] = matchOne(DEEP_LINK)

  private def matchOne(rel: String): Option[String] = pairs
    .filter(p => p._1.equals(rel))
    .map(p => p._2)
    .headOption

  private def matchMore(rel: String): Set[String] = pairs
    .filter(p => p._1.equals(rel))
    .map(p => p._2)
    .toSet

  private def isPagedFeed(rel: String): Boolean = rel match {
    case FIRST    => true
    case LAST     => true
    case PREVIOUS => true
    case NEXT     => true
    case _        => false
  }

  private def isArchivedFeed(rel: String): Boolean = rel match {
    case PREV_ARCHIVE => true
    case NEXT_ARCHIVE => true
    case CURRENT      => true
    case _            => false
  }

}
