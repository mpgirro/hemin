package hemin.engine.parser

import hemin.engine.model.AtomLink

object SemanticLinkParser {

  // Atom Syndication Format (RFC 4287)
  val ALTERNATE: String = "alternate"  // alternate representation of the entry or feed
  val ENCLOSURE: String = "enclosure"  // related resource (media file)
  val RELATED: String   = "related"    // document related to the entry or feed
  val SELF: String      = "self"       // the feed itself
  val VIA: String       = "via"        // source of the information provided in the entry

  // Paged Feed (RFC 5005)
  val FIRST: String    = "first"       // furthest preceding document in a series of documents
  val LAST: String     = "last"        // furthest following document in a series of documents
  val PREVIOUS: String = "previous"    // immediately preceding document in a series of documents
  val NEXT: String     = "next"        // immediately following document in a series of documents

  // Archived Feed (RFC 5005)
  val PREV_ARCHIVE: String = "prev-archive"  // immediately preceding archive document
  val NEXT_ARCHIVE: String = "next-archive"  // immediately following archive document
  val CURRENT: String      = "current"       // feed document containing the most recent entries

  // Podlove Deep Link
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
class SemanticLinkParser (links: List[AtomLink]) {

  import hemin.engine.parser.SemanticLinkParser._

  private val rels: Set[String] = links
    .filter(_.rel.isDefined)
    .flatMap(_.rel)
    .toSet


  val pagedFeeds: Set[String] = rels.filter(isPagedFeed)
  val pagedFeedFirst: Option[String] = pagedFeeds.find(isPagedFeedFirst)
  val archivedFeeds: Set[String] = rels.filter(isArchivedFeed)

  private def isPagedFeed(rel: String): Boolean = rel match {
    case FIRST    => true
    case LAST     => true
    case PREVIOUS => true
    case NEXT     => true
    case _        => false
  }

  private def isPagedFeedFirst(rel: String): Boolean = rel match {
    case FIRST => true
    case _     => false
  }

  private def isArchivedFeed(rel: String): Boolean = rel match {
    case PREV_ARCHIVE => true
    case NEXT_ARCHIVE => true
    case CURRENT      => true
    case _            => false
  }

  private def isPodloveDeepLink(rel: String): Boolean = rel match {
    case DEEP_LINK => true
    case _         => false
  }

}
