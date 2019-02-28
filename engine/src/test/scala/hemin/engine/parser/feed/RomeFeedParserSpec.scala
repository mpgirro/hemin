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

  val expectedPodcastAtomLinks = 8
  val expectedPodcastPersonaAuthors = 0
  val expectedPodcastPersonaContributors = 0
  val expectedEpisodes = 2
  val expectedEpisodeAtomLinks = 1
  val expectedEpisodePersonaAuthors = 0
  val expectedEpisodePersonaContributors = 0
  val expectedChapters = 3

  val parseFailureMsg = "The RomeFeedParser failed to instantiate from the XML feed data"

  "The RomeFeedParser" should "instantiate from a valid XML feed" in {
    RomeFeedParser.parse(feedData) match {
      case Success(_)  => succeed // all is well
      case Failure(ex) => fail(parseFailureMsg)
    }
  }

  it should "extract all Podcast metadata fields" in {
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        Option(parser.podcast)
          .map { p =>
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
            p.atom.links.size shouldBe expectedPodcastAtomLinks
            p.persona.authors.size shouldBe expectedPodcastPersonaAuthors      // TODO
            p.persona.contributors.size shouldBe expectedPodcastPersonaContributors
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

  it should "extract AtomLink metadata fields for a Podcast" in {
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        val als = parser.podcast.atom.links
        als.headOption match {
          case None => fail("Parser failed to extract an AtomLink for a Podcast")
          case Some(al) =>
            al.rel.toList should contain atMostOneOf (
              "self",
              "alternate",
              "next",
              "first",
              "last",
              "hub",
            )
            al.href.toList should contain atMostOneOf (
              "http://example.org/feed/m4a",
              "http://example.org/feed/mp3",
              "http://example.org/feed/oga",
              "http://example.org/feed/opus",
              "http://example.org/feed/m4a?paged=2",
              "http://example.org/feed/m4a?paged=8",
              "http://test.superfeedr.com",
            )
            al.title.toList should contain atMostOneOf (
              "Lorem Ipsum (MPEG-4 AAC Audio)",
              "Lorem Ipsum (MP3 Audio)",
              "Lorem Ipsum (Ogg Vorbis Audio)",
              "Lorem Ipsum (Ogg Opus Audio)",
            )
            al.typ.toList should contain atMostOneOf (
              "application/rss+xml",
              "application/xml"
            )
            al.hrefResolved shouldBe Some("http://example.org/feed/m4a")
            al.length shouldBe Some(0)
            // TODO set this fields in feed, and test for correct extraction
            al.hrefLang shouldBe None
        }
      case Failure(_) => fail(parseFailureMsg)
    }
  }

  ignore should "extract Author (Person) metadata fields for a Podcast" in {
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        val as = parser.podcast.persona.authors
        as.headOption match {
          case None => fail("Parser failed to extract an Author (Person) for a Podcast")
          case Some(author) =>
            // TODO add these fields to the testfeed --> never seen such in real life feed before
            author.name shouldBe None
            author.email shouldBe None
            author.uri shouldBe None
        }
      case Failure(_) => fail(parseFailureMsg)
    }
  }

  ignore should "extract Contributor (Persons) metadata fields for a Podcast" in {
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        val cs = parser.podcast.persona.contributors
        cs.headOption match {
          case None => fail("Parser failed to extract a Contributor (Person) for a Podcast")
          case Some(c) =>
            // TODO add these fields to the testfeed --> never seen such in real life feed before
            c.name shouldBe None
            c.email shouldBe None
            c.uri shouldBe None
        }
      case Failure(_) => fail(parseFailureMsg)
    }
  }

  it should "extract all Episode metadata fields" in {
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
            e.chapters.size shouldBe expectedChapters
            e.atom.links.size shouldBe expectedEpisodeAtomLinks
            e.persona.authors.size shouldBe expectedEpisodePersonaAuthors
            e.persona.contributors.size shouldBe expectedEpisodePersonaContributors
            e.registration.timestamp shouldBe empty
        }
      case Failure(_) => fail(parseFailureMsg)
    }
  }

  it should "extract all Chapter metadata fields" in {
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

  it should "extract all AtomLinks metadata fields for an Episode" in {
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        parser.episodes.headOption match {
          case None => fail("Parser failed to extract an Episode")
          case Some(e) =>
            val als = e.atom.links
            als.headOption match {
              case None => fail("Parser failed to extract an AtomLink for an Episode")
              case Some(al) =>
                al.rel shouldBe Some("http://podlove.org/deep-link")
                al.href.toList should contain atMostOneOf (
                  "http://example.org/episode1",
                  "http://example.org/episode2",
                )
                al.hrefResolved shouldBe Some("http://example.org/episode1")
                al.length shouldBe Some(0)
                // TODO set this fields in feed, and test for correct extraction
                al.hrefLang shouldBe None
                al.title shouldBe None
                al.typ shouldBe None
            }
        }
      case Failure(_) => fail(parseFailureMsg)
    }
  }

  ignore should "extract Author (Person) metadata fields for an Episode" in {
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        parser.episodes.headOption match {
          case None => fail("Parser failed to extract an Episode")
          case Some(e) =>
            val as = e.persona.authors
            as.headOption match {
              case None => fail("Parser failed to extract an Author (Person) for a Podcast")
              case Some(author) =>
                // TODO add these fields to the testfeed --> never seen such in real life feed before
                author.name shouldBe None
                author.email shouldBe None
                author.uri shouldBe None
            }
        }
      case Failure(_) => fail(parseFailureMsg)
    }
  }

  ignore should "extract Contributor (Persons) metadata fields for an Episode" in {
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        parser.episodes.headOption match {
          case None => fail("Parser failed to extract an Episode")
          case Some(e) =>
            val cs = e.persona.contributors
            cs.headOption match {
              case None => fail("Parser failed to extract a Contributor (Person) for a Podcast")
              case Some(c) =>
                // TODO add these fields to the testfeed --> never seen such in real life feed before
                c.name shouldBe None
                c.email shouldBe None
                c.uri shouldBe None
            }
        }
      case Failure(_) => fail(parseFailureMsg)
    }
  }

}
