package hemin.engine.parser.feed

import hemin.engine.model.AtomLink
import org.scalatest.{FlatSpec, Matchers}

class LinkParserSpec
  extends FlatSpec
    with Matchers {

  val links: List[AtomLink] = List(
    // invalid entries that should not be recognized
    AtomLink(
      rel  = None,
      href = None,
    ),
    AtomLink(
      rel  = None,
      href = Some("http://example.org"),
    ),
    AtomLink(
      rel  = Some("alternate"),
      href = None,
    ),
    // Atom Syndication Format (RFC 4287)
    AtomLink(
      rel  = Some("alternate"),
      href = Some("http://example.org/feed/m4a"),
    ),
    AtomLink(
      rel  = Some("alternate"),
      href = Some("http://example.org/feed/mp3"),
    ),
    AtomLink(
      rel  = Some("related"),
      href = Some("http://example.org"),
    ),
    AtomLink(
      rel  = Some("enclosure"),
      href = Some("http://example.org/episode1.m4a"),
    ),
    AtomLink(
      rel  = Some("self"),
      href = Some("http://example.org/feed/m4a?paged=3"),
    ),
    AtomLink(
      rel  = Some("via"),
      href = Some("http://example.org"),
    ),
    // Paged Feeds (RFC 5005)
    AtomLink(
      rel  = Some("first"),
      href = Some("http://example.org/feed/m4a"),
    ),
    AtomLink(
      rel  = Some("last"),
      href = Some("http://example.org/feed/m4a?paged=8"),
    ),
    AtomLink(
      rel  = Some("next"),
      href = Some("http://example.org/feed/m4a?paged=4"),
    ),
    AtomLink(
      rel  = Some("previous"),
      href = Some("http://example.org/feed/m4a?paged=2"),
    ),
    // Archived Feeds (RFC 5005)
    AtomLink(
      rel  = Some("prev-archive"),
      href = Some("http://example.org/feed/m4a?archive=1"),
    ),
    AtomLink(
      rel  = Some("next-archive"),
      href = Some("http://example.org/feed/m4a?archive=3"),
    ),
    AtomLink(
      rel  = Some("current"),
      href = Some("http://example.org/feed/m4a?archive=2"),
    ),
    // Podlove Deep Link
    AtomLink(
      rel  = Some("http://podlove.org/deep-link"),
      href = Some("http://example.org/episode1"),
    ),
    AtomLink(
      rel  = Some(""),
      href = Some(""),
    ),
    AtomLink(
      rel  = Some(""),
      href = Some(""),
    ),
    AtomLink(
      rel  = Some(""),
      href = Some(""),
    ),
  )

  val parser: LinkParser = new LinkParser(links)

  "The RelLinkParser" should "not find links when no relation is set" in {
    val p: LinkParser = new LinkParser(List(AtomLink(
      rel  = None,
      href = Some("http://example.org/feed/mp3")
    )))
    p.self.size shouldBe 0
    p.self shouldBe None
  }

  it should "not find links when no href is set" in {
    val p: LinkParser = new LinkParser(List(AtomLink(
      rel  = Some("self"),
      href = None
    )))
    p.self.size shouldBe 0
    p.self shouldBe None
  }

  it should "find 'alternative' links" in {
    parser.alternate.size shouldBe 2
    parser.alternate shouldBe Set("http://example.org/feed/m4a","http://example.org/feed/mp3")
  }

  it should "find 'related' links" in {
    parser.related.size shouldBe 1
    parser.related shouldBe Set("http://example.org")
  }

  it should "find 'enclosure' links" in {
    parser.enclosure.size shouldBe 1
    parser.enclosure shouldBe Set("http://example.org/episode1.m4a")
  }

  it should "find a 'self' link" in {
    parser.self shouldBe Some("http://example.org/feed/m4a?paged=3")
  }

  it should "find a 'via' links" in {
    parser.via shouldBe Set("http://example.org")
  }

  it should "find a 'first' paged feed link" in {
    parser.pagedFeedFirst shouldBe Some("http://example.org/feed/m4a")
  }

  it should "find a 'last' paged feed link" in {
    parser.pagedFeedLast shouldBe Some("http://example.org/feed/m4a?paged=8")
  }

  it should "find a 'next' paged feed link" in {
    parser.pagedFeedNext shouldBe Some("http://example.org/feed/m4a?paged=4")
  }

  it should "find a 'previous' paged feed link" in {
    parser.pagedFeedPrevious shouldBe Some("http://example.org/feed/m4a?paged=2")
  }

  it should "find a 'prev-archive' paged feed link" in {
    parser.archivedFeedNext shouldBe Some("http://example.org/feed/m4a?archive=3")
  }

  it should "find a 'next-archive' paged feed link" in {
    parser.archivedFeedPrev shouldBe Some("http://example.org/feed/m4a?archive=1")
  }

  it should "find a 'current' paged feed link" in {
    parser.archivedFeedCurrent shouldBe Some("http://example.org/feed/m4a?archive=2")
  }

  it should "find a Podlove deep-link" in {
    parser.deepLink shouldBe Some("http://example.org/episode1")
  }

}
