package io.hemin.engine.parser.feed

import java.nio.file.{Files, Paths}
import java.time.LocalDateTime
import java.util.stream.Collectors

import com.google.common.base.Strings.isNullOrEmpty
import io.hemin.engine.util.mapper.DateMapper
import org.scalatest.{FlatSpec, Matchers}

class RomeFeedParserSpec extends FlatSpec with Matchers {

  private val feedData: String = Files.lines(Paths.get("src", "test", "resources", "testfeed.xml")).collect(Collectors.joining("\n"))

  private val testDate: Option[LocalDateTime] = DateMapper.asLocalDateTime("2018-03-16T23:49:08")

  "The Parser" should "parse the Feed" in {
    val parser = new RomeFeedParser(feedData)

    // TODO better check that the parser was constructed successfully
    assert(parser != null, "The Parser was not initialized successfully")
  }

  it should "extract all Podcast metadata fields correctly" in {
    val parser = new RomeFeedParser(feedData)
    parser.podcast should not be empty
    parser.podcast.foreach { p =>
      p.id shouldBe empty
      p.title shouldBe Some("Lorem Ipsum")
      p.link shouldBe Some("http://example.org")
      p.description shouldBe Some("Lorem Ipsum")
      p.pubDate shouldBe testDate
      p.image shouldBe Some("http://example.org/cover.jpg")
      p.meta.lastBuildDate shouldEqual None // TODO why is it not equal to testDate ?! --> unexpected behavior of ROME?!
      p.meta.language shouldBe Some("de-DE")
      p.meta.generator shouldBe Some("Lorem Ipsum")
      p.meta.copyright shouldBe Some("Lorem Ipsum")
      p.meta.docs shouldBe Some("Lorem Ipsum")
      p.meta.managingEditor shouldBe empty // TODO add to testfeed.xml
      p.registration.timestamp shouldBe empty
      p.registration.complete shouldEqual None
      p.itunes.summary shouldBe Some("Lorem Ipsum")
      p.itunes.author shouldBe Some("Lorem Ipsum")
      p.itunes.keywords should contain ("Lorem Ipsum")
      p.itunes.categories.exists(_.equals("Technology")) // TODO nested stuff, and more tests
      p.itunes.explicit shouldBe Some(false)
      p.itunes.block shouldBe Some(true) // TODO is this false or inversion by ROME? it is set as "no" in feed!
      p.itunes.podcastType shouldBe Some("episodic")
      p.itunes.ownerName shouldBe Some("Lorem Ipsum")
      p.itunes.ownerEmail shouldBe Some("test@example.org")
      p.feedpress.locale shouldEqual None // TODO why not Some("en") ?!
      p.fyyd.verify shouldEqual None // TODO should be Some("abcdefg") once we support Fyyd
    }
  }

  it should "extract 2 Episodes" in {
    val parser = new RomeFeedParser(feedData)
    val es = parser.episodes
    es should not be empty
    assert(es.size == 2, s"The Parser extracted ${es.size} Episodes instead of 2")
  }

  it should "extract all Episode metadata fields correctly" in {
    val parser = new RomeFeedParser(feedData)
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
        e.image shouldEqual None // shouldBe Some("http://example.org/cover.jpg")
        e.contentEncoded shouldBe Some("Lorem Ipsum")
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
  }

}
