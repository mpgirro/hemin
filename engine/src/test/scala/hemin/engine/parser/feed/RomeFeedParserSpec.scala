package hemin.engine.parser.feed

import java.nio.file.{Files, Paths}
import java.util.stream.Collectors

import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Failure, Success}

class RomeFeedParserSpec
  extends FlatSpec
    with Matchers {

  val feedData: String = Files
    .lines(Paths.get("src", "test", "resources", "testfeed.xml"))
    .collect(Collectors.joining("\n"))

  val testDate: Option[Long] = Some(1521240548000L) // = 2018-03-16T23:49:08

  val expectedEpisodes: Int = 2
  val expectedChapters: Int = 3

  val parseFailureMsg = "A RomeFeedParser for the test feed data could not be instantiated"

  "The RomeFeedParser" should "be able to parse a valid Feed" in {
    RomeFeedParser.parse(feedData) match {
      case Success(_)  => succeed // all is well
      case Failure(ex) => fail(parseFailureMsg)
    }
  }

  it should "extract all Podcast metadata fields correctly" in {
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        Option(parser.podcast)
          .map {p =>
            p.id shouldBe empty
            p.title shouldBe Some("Lorem Ipsum")
            p.link shouldBe Some("http://example.org")
            p.description shouldBe Some("Lorem Ipsum")
            p.pubDate shouldBe testDate
            p.image shouldBe Some("http://example.org/cover.jpg")
            p.lastBuildDate shouldEqual None // TODO why is it not equal to testDate ?! --> unexpected behavior of ROME?!
            p.language shouldBe Some("de-DE")
            p.generator shouldBe Some("Lorem Ipsum")
            p.copyright shouldBe Some("Lorem Ipsum")
            p.docs shouldBe Some("Lorem Ipsum")
            p.managingEditor shouldBe empty // TODO add to testfeed.xml
            p.registration.timestamp shouldBe empty
            p.registration.complete shouldEqual None
            p.itunes.summary shouldBe Some("Lorem Ipsum")
            p.itunes.author shouldBe Some("Lorem Ipsum")
            p.itunes.keywords should contain ("Lorem Ipsum")
            p.itunes.categories.exists(_.equals("Technology")) // TODO nested stuff, and more tests
            p.itunes.explicit shouldBe Some(false)
            p.itunes.block shouldBe Some(false)
            p.itunes.typ shouldBe Some("episodic")
            p.itunes.ownerName shouldBe Some("Lorem Ipsum")
            p.itunes.ownerEmail shouldBe Some("test@example.org")
            p.atom.links.size shouldBe 8
            p.persona.authors.size shouldBe 0      // TODO
            p.persona.contributors.size shouldBe 0 // TODO
            p.feedpress.locale shouldEqual None // TODO why not Some("en") ?!
            p.fyyd.verify shouldEqual None // TODO should be Some("abcdefg") once we support Fyyd
          }
          .orElse({
            fail("The Parser produced NULL for the Podcast")
            None
          })
      case Failure(_) => fail(parseFailureMsg)
    }
  }

  it should "extract all Episodes from the feed" in {
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        val es = parser.episodes
        assert(es.size == expectedEpisodes, s"The Parser extracted ${es.size} Episodes instead of $expectedEpisodes")
      case Failure(_) => fail(parseFailureMsg)
    }
  }

  it should "extract all Episode metadata fields correctly" in {
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        parser.episodes.headOption match {
          case None => fail("Parser failed to extract an Episode")
          case Some(e) =>
            e.id shouldBe empty
            e.podcastId shouldBe empty
            e.title shouldBe Some("Lorem Ipsum")
            e.podcastTitle shouldBe Some("Lorem Ipsum")
            e.link shouldBe Some("http://example.org/episode1")
            e.pubDate shouldBe testDate
            e.guid shouldBe Some("1fa609024fdf097")
            e.guidIsPermalink shouldEqual None
            e.description shouldBe Some("Lorem Ipsum")
            e.image shouldBe Some("http://example.org/cover.jpg")
            e.contentEncoded shouldBe Some("Lorem Ipsum")
            e.chapters should not be empty
            e.itunes.duration shouldBe Some("03:24:27")
            e.itunes.subtitle shouldBe Some("Lorem Ipsum")
            e.itunes.author shouldBe Some("Lorem Ipsum")
            e.itunes.summary shouldBe Some("Lorem Ipsum")
            e.itunes.season shouldBe Some(1)
            e.itunes.episode shouldBe Some(1)
            e.itunes.episodeType shouldBe Some("full")
            e.enclosure.url shouldBe Some("http://example.org/episode1.m4a")
            e.enclosure.length shouldBe Some(78589133)
            e.enclosure.typ shouldBe Some("audio/mp4")
            e.atom.links.size shouldBe 0           // TODO
            e.persona.authors.size shouldBe 0      // TODO
            e.persona.contributors.size shouldBe 0 // TODO
            e.registration.timestamp shouldBe empty
        }
      case Failure(_) => fail(parseFailureMsg)
    }
  }

  it should "extract all Chapters from an Episode" in {
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        parser.episodes.headOption match {
          case None => fail("Parser failed to extract an Episode")
          case Some(e) =>
            val cs = e.chapters
            assert(cs.size == expectedChapters, s"The Parser extracted ${cs.size} Chapters instead of $expectedChapters")
        }
      case Failure(_) => fail(parseFailureMsg)
    }
  }


  it should "extract all Chapter metadata fields correctly" in {
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        parser.episodes.headOption match {
          case None => fail("Parser failed to extract an Episode")
          case Some(e) =>
            val cs = e.chapters
            cs.headOption match {
              case None => fail("Parser failed to extract a Chapter")
              case Some(c) =>
                c.start shouldBe Some("00:00:00.000")
                c.title shouldBe Some("Lorem Ipsum")
                c.href shouldBe Some("http://example.org")
                c.image shouldBe Some("http://example.org/cover") // TODO this is None for some reason
            }
        }
      case Failure(_) => fail(parseFailureMsg)
    }
  }

  // TODO test atom links extraction like chapters

  // TODO test persona extraction like chapters

}
