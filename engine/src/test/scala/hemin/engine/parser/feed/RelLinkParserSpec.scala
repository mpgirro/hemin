package hemin.engine.parser.feed

import java.nio.file.{Files, Paths}
import java.util.stream.Collectors

import hemin.engine.model.AtomLink
import org.scalatest.{FlatSpec, Matchers}

class RelLinkParserSpec
  extends FlatSpec
    with Matchers {

  /*
  val links: List[AtomLink] = {
    val atomXml: String = Files
      .lines(Paths.get("src", "test", "resources", "atom.xml"))
      .collect(Collectors.joining("\n"))

    val feedParser: FeedParser = RomeFeedParser.parse(atomXml).get
    val xs = feedParser.podcast.atom.links
    val ys = feedParser.episodes.head.atom.links
    xs ++ ys
  }
  */

  val links: List[AtomLink] = List(
    AtomLink(
      rel  = None,
      href = None,
    ),
    AtomLink(
      rel  = None,
      href = Some("http://example.org"),
    ),
    AtomLink(
      rel  = Some("self"),
      href = Some(""),
    ),
    AtomLink(
      rel  = Some("alternate"),
      href = Some("http://example.org/feed/m4a"),
    ),
    AtomLink(
      rel  = Some("alternate"),
      href = Some("http://example.org/feed/mp3"),
    ),
    AtomLink(
      rel  = Some("enclosure"),
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
    AtomLink(
      rel  = Some(""),
      href = Some(""),
    ),
  )

  val parser: RelLinkParser = new RelLinkParser(links)

  "The LinkParser" should "find alternative links" in {
    parser.alternate.size shouldBe 3
  }

  it should "find enclosure links" in {
    parser.enclosure.size shouldBe 1
    parser.enclosure.head shouldBe "http://example.org/episode1"
  }

}
