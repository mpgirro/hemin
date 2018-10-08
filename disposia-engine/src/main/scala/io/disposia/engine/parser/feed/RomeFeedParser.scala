package io.disposia.engine.parser.feed

import java.io.StringReader

import com.google.common.base.Strings.isNullOrEmpty
import com.rometools.modules.itunes.FeedInformation
import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed, SyndImage}
import com.rometools.rome.io.SyndFeedInput
import com.typesafe.scalalogging.Logger
import io.disposia.engine.domain.info._
import io.disposia.engine.domain.{Chapter, Episode, Podcast}
import io.disposia.engine.parse.rss.RomeModuleExtractor
import io.disposia.engine.util.mapper.{DateMapper, UrlMapper}
import org.xml.sax.InputSource

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._


// TODO rewrite this class in idiomatic Scala
class RomeFeedParser(private val xmlData: String) {

  private val log: Logger = Logger(getClass)

  private val inputSource: InputSource = new InputSource(new StringReader(xmlData))
  private val input: SyndFeedInput = new SyndFeedInput
  private val feed: SyndFeed = input.build(inputSource)
  private val feedItunesModule: Option[FeedInformation] = RomeModuleExtractor.getItunesModule(feed).asScala

  val podcast: Podcast = parseFeed(feed)
  val episodes: List[Episode] = extractEpisodes(feed)

  /*
  try {

    // TODO

  } catch {
    case ex: Throwable =>
      log.error("RomeFeedParser could not parse the feed : {}", ex.getMessage)
      ex.printStackTrace()
  }
  */

  private def parseFeed(feed: SyndFeed): Podcast = {

    // # # # # # # # # # # # # # # # # # # # # # # #

    // TODO process the atomlinks
    RomeModuleExtractor.getAtomLinks(feed).asScala
      .foreach { atomLink =>
        if (atomLink.getRel == "http://podlove.org/deep-link") {
          // TODO this should be a link to the episode website (but is it always though?!)
        }
        else if (atomLink.getRel == "payment") {
          // TODO
        }
        else if (atomLink.getRel == "self") {
          // TODO
        }
        else if (atomLink.getRel == "alternate") {
          // TODO
        }
        else if (atomLink.getRel == "first") {
          // TODO
        }
        else if (atomLink.getRel == "next") {
          // TODO
        }
        else if (atomLink.getRel == "last") {
          // TODO
        }
        else if (atomLink.getRel == "hub") {
          // TODO
        }
        else if (atomLink.getRel == "search") {
          // TODO
        }
        else if (atomLink.getRel == "via") {
          // TODO
        }
        else if (atomLink.getRel == "related") {
          // TODO
        }
        else if (atomLink.getRel == "prev-archive") {
          // TODO
        }
        else log.warn("Came across an <atom:link> with a relation I do not handle : '{}'", atomLink.getRel)
      }

    Podcast(
      id              = None,
      title           = podcastTitleWithImageFallback,
      link            = podcastLinkWithImageFallback,
      description     = podcastDescriptionWithImageFallback,
      pubDate         = DateMapper.asLocalDateTime(feed.getPublishedDate),
      image           = podcastImageWithItunesFallback,
      meta = PodcastMetadata(
        lastBuildDate  = None,  // TODO the parser does not yet produce this
        language       = Option(feed.getLanguage),
        generator      = Option(feed.getGenerator),
        copyright      = Option(feed.getCopyright),
        docs           = Option(feed.getDocs),
        managingEditor = Option(feed.getManagingEditor),
      ),
      registration = PodcastRegistrationInfo(
        timestamp = None,
        complete  = None,
      ),
      itunes    = podcastItunesInfo,
      feedpress = podcastFeedpressInfo,
      fyyd      = podcastFyydInfo,
    )
  }

  private def extractEpisodes(feed: SyndFeed): List[Episode] = feed
    .getEntries.asScala
    .map(e => extractEpisode(e))
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
    chapters        = episodeChapters(e),
    itunes          = episodeItunesInfo(e),
    enclosure       = episodeEnclosureInfo(e),
    registration = EpisodeRegistrationInfo(
      timestamp = None,
    )
  )

  private def podcastTitleWithImageFallback: Option[String] = {
    val feedTitle = Option(feed.getTitle)
    val imgTitle = Option(feed.getImage).map(_.getTitle)

    (feedTitle, imgTitle) match {
      case (Some(_), _)    => feedTitle
      case (None, Some(_)) => imgTitle
      case (_, _)          => None
    }
  }

  private def podcastLinkWithImageFallback: Option[String] = {
    val feedLink = Option(feed.getLink)
    val imgLink = Option(feed.getImage).map(_.getLink)

    val link = (feedLink, imgLink) match {
      case (Some(_), _)    => feedLink
      case (None, Some(_)) => imgLink
      case (_, _)          => None
    }
    link.flatMap(UrlMapper.sanitize)
  }

  private def podcastDescriptionWithImageFallback: Option[String] = {
    val feedDescr = Option(feed.getDescription)
    val imgDescr = Option(feed.getImage).map(_.getDescription)

    (feedDescr, imgDescr) match {
      case (Some(_), _)    => feedDescr
      case (None, Some(_)) => imgDescr
      case (_, _)          => None
    }
  }

  private def podcastImageWithItunesFallback: Option[String] = {
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

  private def podcastItunesInfo: PodcastItunesInfo = feedItunesModule
    .map { itunes =>
      PodcastItunesInfo(
        summary     = Option(itunes.getSummary),
        author      = Option(itunes.getAuthor),
        keywords    = Option(itunes.getKeywords)
          .map(_.toList)
          .getOrElse(Nil),
        categories  = Option(itunes.getCategories)
          .map(cs => cs.asScala.map(c => c.getName))
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

  private def podcastFeedpressInfo: PodcastFeedpressInfo = PodcastFeedpressInfo(
    locale = None
  )

  private def podcastFyydInfo: PodcastFyydInfo = PodcastFyydInfo(
    verify = None
  )

  private def episodeImage(e: SyndEntry): Option[String] = RomeModuleExtractor
    .getItunesEntryInformation(e).asScala
    .flatMap { itunes =>
      Option(itunes.getImage).map(img => img.toExternalForm)
    }

  private def episodeItunesInfo(e: SyndEntry): EpisodeItunesInfo = RomeModuleExtractor
    .getItunesEntryInformation(e).asScala
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

  private def episodeEnclosureInfo(e: SyndEntry): EpisodeEnclosureInfo = Option(e.getEnclosures)
    .map { enclosures =>
      if (enclosures.size > 1) log.warn("Encountered multiple <enclosure> elements in <item> element")
      if (enclosures.size > 0) {
        val enclosure = e.getEnclosures.get(0)
        EpisodeEnclosureInfo(
          url    = Option(enclosure.getUrl),
          length = Option(enclosure.getLength),
          typ    = Option(enclosure.getType),
        )
      } else {
        EpisodeEnclosureInfo()
      }
    }.getOrElse(EpisodeEnclosureInfo())

  private def episodeContentEncoded(e: SyndEntry): Option[String] = RomeModuleExtractor
    .getContentModule(e).asScala
    .map { content =>
      if (content.getEncodeds.size > 1) log.warn("Encountered multiple <content:encoded> elements in <item> element")
      if (content.getEncodeds.size > 0) {
        content.getEncodeds.get(0)
      } else {
        null
      }
    }

  private def episodeChapters(e: SyndEntry): List[Chapter] = RomeModuleExtractor
    .getPodloveSimpleChapterModule(e).asScala
    .map(simpleChapters => simpleChapters.getChapters)
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
    }.getOrElse(List())

}
