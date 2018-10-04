package io.disposia.engine.parser

import java.io.StringReader

import com.google.common.base.Strings.isNullOrEmpty
import com.rometools.modules.itunes.FeedInformation
import com.rometools.rome.feed.module.Module
import com.rometools.rome.feed.synd.{SyndFeed, SyndImage}
import com.rometools.rome.io.SyndFeedInput
import com.rometools.modules.itunes.FeedInformation
import com.typesafe.scalalogging.Logger
import io.disposia.engine.mapper.DateMapper
import io.disposia.engine.newdomain.podcast.{PodcastFeedpressInfo, PodcastItunesInfo, PodcastMetadata}
import io.disposia.engine.newdomain.{NewEpisode, NewPodcast}
import io.disposia.engine.util.UrlUtil
import org.xml.sax.InputSource

import scala.collection.JavaConverters._


class NewRomeFeedParser (private val xmlData: String) {

  private val log: Logger = Logger(getClass)

  private val inputSource: InputSource = new InputSource(new StringReader(xmlData))
  private val input: SyndFeedInput = new SyndFeedInput
  private val feed: SyndFeed = input.build(inputSource)

  val podcast: NewPodcast = parseFeed(feed)
  val episodes: List[NewEpisode] = extractEpisodes(feed)

  /*
  try {

    // TODO

  } catch {
    case ex: Throwable =>
      log.error("RomeFeedParser could not parse the feed : {}", ex.getMessage)
      ex.printStackTrace()
  }
  */

  private def fromSyndImage(src: SyndImage): Option[String] = Option(src)
    .flatMap { img: SyndImage => if (isNullOrEmpty(img.getUrl)) None else Some(img.getUrl) }

  private def withTitleFallback(p: NewPodcast, i: SyndImage): NewPodcast = {
    if (p.title.isEmpty && !isNullOrEmpty(i.getTitle)) {
      p.copy(title = Option(i.getTitle))
    } else {
      p
    }
  }

  private def withLinkFallback(p: NewPodcast, i: SyndImage): NewPodcast = {
    if (p.link.isEmpty && !isNullOrEmpty(i.getLink)) {
      p.copy(link = Option(UrlUtil.sanitize(i.getLink)))
    } else {
      p
    }
  }

  private def withDescriptionFallback(p: NewPodcast, i: SyndImage): NewPodcast = {
    if (p.description.isEmpty && !isNullOrEmpty(i.getDescription)) {
      p.copy(description = Option(i.getDescription))
    } else {
      p
    }
  }

  private def fromItunesModule(itunesFeedModule: Module): PodcastItunesInfo = {
    val itunes: FeedInformation = itunesFeedModule.asInstanceOf[FeedInformation]
    if (itunes != null) PodcastItunesInfo(
      summary    = Option(itunes.getSummary),
      author     = Option(itunes.getAuthor),
      keywords   = Option(itunes.getKeywords),
      categories = Option(itunes.getCategories).map(cs => cs.asScala.map(c => c.getName).toSet),
      explicit   = Option(itunes.getExplicit),
      block      = Option(itunes.getBlock),
      podcastType = Option(itunes.getType),
      ownerName  = Option(itunes.getOwnerName),
      ownerEmail = Option(itunes.getOwnerEmailAddress),
    ) else PodcastItunesInfo()
  }

  private def parseFeed(feed: SyndFeed): NewPodcast = {
    NewPodcast(
      title = Option(feed.getTitle),
      link = Option(UrlUtil.sanitize(feed.getLink)),
      description = Option(feed.getDescription),
      image = fromSyndImage(feed.getImage), // TODO or use itunesImage
      pubDate = Option(DateMapper.INSTANCE.asLocalDateTime(feed.getPublishedDate)),
      meta = PodcastMetadata(
        language = Option(feed.getLanguage),
        generator = Option(feed.getGenerator),
        copyright = Option(feed.getCopyright),
        docs = Option(feed.getDocs),
        managingEditor = Option(feed.getManagingEditor),
      ),
      //itunes = fromItunesModule(feed.getModule(FeedInformation.URI)), // TODO
    )
  }

  private def extractEpisodes(feed: SyndFeed): List[NewEpisode] = {
    // TODO
    List()
  }

}
