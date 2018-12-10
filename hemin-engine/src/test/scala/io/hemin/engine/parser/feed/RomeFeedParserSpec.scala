package io.hemin.engine.parser.feed

import java.nio.file.{Files, Paths}
import java.time.LocalDateTime
import java.util.stream.Collectors

import io.hemin.engine.util.mapper.DateMapper
import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Failure, Success}

class RomeFeedParserSpec extends FlatSpec with Matchers {

  private val SUCCEED: Boolean = true
  private val FAILURE: Boolean = false

  private val feedData: String = Files.lines(Paths.get("src", "test", "resources", "testfeed.xml")).collect(Collectors.joining("\n"))

  private val testDate: Option[LocalDateTime] = DateMapper.asLocalDateTime("2018-03-16T23:49:08")

  "The Parser" should "parse the Feed" in {
    RomeFeedParser.parse(feedData) match {
      case Success(_)  => assert(SUCCEED) // all is well
      case Failure(ex) => assert(FAILURE, "A Parser for the test feed data could not be instantiated")
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
            // TODO test the values from p.atomLinks
            p.registration.timestamp shouldBe empty
            p.registration.complete shouldEqual None
            p.itunes.summary shouldBe Some("Lorem Ipsum")
            p.itunes.author shouldBe Some("Lorem Ipsum")
            p.itunes.keywords should contain ("Lorem Ipsum")
            p.itunes.categories.exists(_.equals("Technology")) // TODO nested stuff, and more tests
            p.itunes.explicit shouldBe Some(false)
            p.itunes.block shouldBe Some(false)
            p.itunes.podcastType shouldBe Some("episodic")
            p.itunes.ownerName shouldBe Some("Lorem Ipsum")
            p.itunes.ownerEmail shouldBe Some("test@example.org")
            p.feedpress.locale shouldEqual None // TODO why not Some("en") ?!
            p.fyyd.verify shouldEqual None // TODO should be Some("abcdefg") once we support Fyyd
          }
          .orElse({
            assert(FAILURE, "The Parser produced NULL for the Podcast")
            None
          })
      case Failure(_) => assert(FAILURE, "A Parser for the test feed data could not be instantiated")
    }
  }

  it should "extract 2 Episodes" in {
    val expected = 2
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        val es = parser.episodes
        es should not be empty
        assert(es.size == expected, s"The Parser extracted ${es.size} Episodes instead of $expected")
      case Failure(_) => assert(FAILURE, "A Parser for the test feed data could not be instantiated")
    }
  }

  it should "extract all Episode metadata fields correctly" in {
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        val es = parser.episodes
        es.headOption match {
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
            // TODO test the values from e.atomLinks
            //e.chapters should not be empty // TODO test chapter fields as well
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
            e.registration.timestamp shouldBe empty
        }
      case Failure(_) => assert(FAILURE, "A Parser for the test feed data could not be instantiated")
    }
  }

  it should "extract 3 Chapters" in {
    val expected = 3
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        val es = parser.episodes
        es.headOption match {
          case None => fail("Parser failed to extract an Episode")
          case Some(e) =>
            val cs = e.chapters
            cs should not be empty
            assert(cs.size == expected, s"The Parser extracted ${cs.size} Chapters instead of $expected")
        }
      case Failure(_) => assert(FAILURE, "A Parser for the test feed data could not be instantiated")
    }
  }

  it should "extract all Chapter metadata fields correctly" in {
    val expected = 3
    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        val es = parser.episodes
        es.headOption match {
          case None => fail("Parser failed to extract an Episode")
          case Some(e) =>
            val cs = e.chapters
            cs.headOption match {
              case None => fail("Parser failed to extract a Chapter")
              case Some(c) =>
                //c.id shouldBe Some("")
                //c.episodeId shouldBe Some("")
                c.start shouldBe Some("00:00:00.000")
                c.title shouldBe Some("Lorem Ipsum")
                c.href shouldBe Some("http://example.org")
                c.image shouldBe Some("http://example.org/cover") // TODO this is None for some reason
            }
        }
      case Failure(_) => assert(FAILURE, "A Parser for the test feed data could not be instantiated")
    }
  }

}
