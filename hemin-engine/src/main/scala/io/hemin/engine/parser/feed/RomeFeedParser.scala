package io.hemin.engine.parser.feed

import java.io.StringReader

import com.google.common.base.Strings.isNullOrEmpty
import com.rometools.modules.itunes.FeedInformation
import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed, SyndImage}
import com.rometools.rome.io.SyndFeedInput
import com.typesafe.scalalogging.Logger
import io.hemin.engine.model.info._
import io.hemin.engine.model.{AtomLink, Chapter, Episode, Podcast}
import io.hemin.engine.util.mapper.{DateMapper, UrlMapper}
import org.xml.sax.InputSource

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._
import scala.util.Try

object RomeFeedParser {
  def parse(xmlData: String): Try[RomeFeedParser] = Try(new RomeFeedParser(xmlData))
}

class RomeFeedParser private (private val xmlData: String) {

  private val log: Logger = Logger(getClass)

  private val inputSource: InputSource = new InputSource(new StringReader(xmlData))
  private val input: SyndFeedInput = new SyndFeedInput
  private val feed: SyndFeed = input.build(inputSource)
  private val feedItunesModule: Option[FeedInformation] = RomeModuleExtractor.getItunesModule(feed).asScala

  val podcast: Podcast = parseFeed(feed)
  val episodes: List[Episode] = extractEpisodes(feed)

  private def parseFeed(feed: SyndFeed): Podcast = Podcast(
    id              = None,
    title           = podcastTitleWithImageFallback,
    link            = podcastLinkWithImageFallback,
    description     = podcastDescriptionWithImageFallback,
    pubDate         = DateMapper.asLocalDateTime(feed.getPublishedDate),
    image           = podcastImageWithItunesFallback,
    lastBuildDate   = None,  // TODO the parser does not yet produce this
    language        = Option(feed.getLanguage),
    generator       = Option(feed.getGenerator),
    copyright       = Option(feed.getCopyright),
    docs            = Option(feed.getDocs),
    managingEditor  = Option(feed.getManagingEditor),
    atomLinks       = podcastAtomLinks,
    registration = PodcastRegistrationInfo(
      timestamp = None,
      complete  = None,
    ),
    itunes    = podcastItunesInfo,
    feedpress = podcastFeedpressInfo,
    fyyd      = podcastFyydInfo,
  )

  private def extractEpisodes(feed: SyndFeed): List[Episode] = feed
    .getEntries
    .asScala
    .map(extractEpisode)
    .toList

  private def extractEpisode(e: SyndEntry): Episode = Episode(
    id              = None,
    podcastId       = None,
    podcastTitle    = podcast.title,
    title           = Option(e.getTitle),
    link            = UrlMapper.sanitize(e.getLink),
    pubDate         = DateMapper.asLocalDateTime(e.getPublishedDate),
    guid            = Option(e.getUri),
    guidIsPermalink = None, // TODO welches feld von ROME korrespondiert zu diesem Boolean?
    description     = Option(e.getDescription).map(_.getValue),
    image           = episodeImage(e),
    contentEncoded  = episodeContentEncoded(e),
    atomLinks       = episodeAtomLinks(e),
    chapters        = episodeChapters(e),
    itunes          = episodeItunesInfo(e),
    enclosure       = episodeEnclosureInfo(e),
    registration = EpisodeRegistrationInfo(
      timestamp = None,
    )
  )

  private lazy val podcastTitleWithImageFallback: Option[String] = {
    val feedTitle = Option(feed.getTitle)
    val imgTitle = Option(feed.getImage).map(_.getTitle)

    (feedTitle, imgTitle) match {
      case (Some(_), _)    => feedTitle
      case (None, Some(_)) => imgTitle
      case (_, _)          => None
    }
  }

  private lazy val podcastLinkWithImageFallback: Option[String] = {
    val feedLink = Option(feed.getLink)
    val imgLink = Option(feed.getImage).map(_.getLink)

    val link = (feedLink, imgLink) match {
      case (Some(_), _)    => feedLink
      case (None, Some(_)) => imgLink
      case (_, _)          => None
    }
    link.flatMap(UrlMapper.sanitize)
  }

  private lazy val podcastDescriptionWithImageFallback: Option[String] = {
    val feedDescr = Option(feed.getDescription)
    val imgDescr = Option(feed.getImage).map(_.getDescription)

    (feedDescr, imgDescr) match {
      case (Some(_), _)    => feedDescr
      case (None, Some(_)) => imgDescr
      case (_, _)          => None
    }
  }

  private lazy val podcastImageWithItunesFallback: Option[String] = {
    val feedImg: Option[String] = Option(feed.getImage)
      .flatMap { img: SyndImage =>
        if (isNullOrEmpty(img.getUrl))
          None
        else
          Some(img.getUrl)
      }

    val itunesImg: Option[String] = feedItunesModule
      .map(_.getImage)
      .map(_.toString)
      .orElse(None)

    (feedImg, itunesImg) match {
      case (Some(_), _)    => feedImg
      case (None, Some(_)) => itunesImg
      case (_, _)          => None
    }
  }

  private lazy val podcastItunesInfo: PodcastItunesInfo = feedItunesModule
    .map { itunes =>
      PodcastItunesInfo(
        summary     = Option(itunes.getSummary),
        author      = Option(itunes.getAuthor),
        keywords    = Option(itunes.getKeywords)
          .map(_.toList)
          .getOrElse(Nil),
        categories  = Option(itunes.getCategories)
          .map(_.asScala.map(_.getName))
          .map(_.toSet)  // eliminate duplicates
          .map(_.toList)
          .getOrElse(Nil),
        explicit    = Option(itunes.getExplicit),
        block       = Option(itunes.getBlock),
        podcastType = Option(itunes.getType),
        ownerName   = Option(itunes.getOwnerName),
        ownerEmail  = Option(itunes.getOwnerEmailAddress),
      )
    }.getOrElse(PodcastItunesInfo())

  private lazy val podcastFeedpressInfo: PodcastFeedpressInfo = PodcastFeedpressInfo(locale = None)

  private lazy val podcastFyydInfo: PodcastFyydInfo = PodcastFyydInfo(verify = None)

  private lazy val podcastAtomLinks: List[AtomLink] = RomeModuleExtractor
    .getAtomLinks(feed)
    .asScala
    .map(AtomLink.fromRome)
    .toList

  private def episodeAtomLinks(e: SyndEntry): List[AtomLink] = RomeModuleExtractor
    .getAtomLinks(e)
    .asScala
    .map(AtomLink.fromRome)
    .toList

  private def episodeImage(e: SyndEntry): Option[String] = RomeModuleExtractor
    .getItunesEntryInformation(e)
    .asScala
    .flatMap(itunes => Option(itunes.getImage))
    //.map(_.getImage) // TODO why is this line not working, but the above?
    .map(_.toExternalForm)
    .orElse(podcast.image) // fallback is the podcast's image

  private def episodeItunesInfo(e: SyndEntry): EpisodeItunesInfo = RomeModuleExtractor
    .getItunesEntryInformation(e)
    .asScala
    .map { itunes =>
      EpisodeItunesInfo(
        duration    = Option(itunes.getDuration).map(_.toString),
        subtitle    = Option(itunes.getSubtitle),
        author      = Option(itunes.getAuthor),
        summary     = Option(itunes.getSummary),
        season      = Option(itunes.getSeason),
        episode     = Option(itunes.getEpisode),
        episodeType = Option(itunes.getEpisodeType),
      )
    }.getOrElse(EpisodeItunesInfo())

  private def episodeEnclosureInfo(entry: SyndEntry): EpisodeEnclosureInfo = Option(entry.getEnclosures)
    .map { es =>
      if (es.size > 1) log.warn("Encountered multiple <enclosure> elements in <item> element")
      if (es.size > 0) {
        val e = entry.getEnclosures.get(0)
        EpisodeEnclosureInfo(
          url    = Option(e.getUrl),
          length = Option(e.getLength),
          typ    = Option(e.getType),
        )
      } else {
        EpisodeEnclosureInfo()
      }
    }.getOrElse(EpisodeEnclosureInfo())

  private def episodeContentEncoded(e: SyndEntry): Option[String] = RomeModuleExtractor
    .getContentModule(e)
    .asScala
    .map { content =>
      val es = content.getEncodeds
      if (es.size > 1) log.warn("Encountered multiple <content:encoded> elements in <item> element")
      if (es.size > 0) {
        es.get(0)
      } else {
        null
      }
    }
    .flatMap(Option(_)) // re-wrap, because we produce null in the map above

  private def episodeChapters(e: SyndEntry): List[Chapter] = RomeModuleExtractor
    .getPodloveSimpleChapterModule(e)
    .asScala
    .map(_.getChapters)
    .map { _.asScala
      .map { c =>
        Chapter(
          id        = None,
          episodeId = None,
          start     = Option(c.getStart),
          title     = Option(c.getTitle),
          href      = Option(c.getHref),
          image     = Option(c.getImage),
        )
      }.toList
    }.getOrElse(Nil)

}
