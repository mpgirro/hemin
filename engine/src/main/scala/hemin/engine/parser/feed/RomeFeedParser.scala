package hemin.engine.parser.feed

import java.io.StringReader

import com.google.common.base.Strings.isNullOrEmpty
import com.rometools.modules.itunes.FeedInformation
import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed}
import com.rometools.rome.io.SyndFeedInput
import com.typesafe.scalalogging.Logger
import hemin.engine.model._
import hemin.engine.util.mapper.{DateMapper, UrlMapper}
import org.xml.sax.InputSource

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._
import scala.util.Try

class RomeFeedParser
  extends FeedParser {

  private val log: Logger = Logger(getClass)

  override def parse(xmlData: String): Try[FeedParserResult] = Try {
    log.debug("Parsing Feed XML data")

    val inputSource: InputSource = new InputSource(new StringReader(xmlData))
    val input: SyndFeedInput = new SyndFeedInput
    val feed: SyndFeed = input.build(inputSource)

    val podcast: Podcast = parseFeed(feed)
    val episodes: List[Episode] = extractEpisodes(feed, podcast)

    FeedParserResult(podcast, episodes)
  }

  private def parseFeed(feed: SyndFeed): Podcast = Podcast(
    id             = None,
    title          = podcastTitleWithImageFallback(feed),
    link           = podcastLinkWithImageFallback(feed),
    description    = podcastDescriptionWithImageFallback(feed),
    pubDate        = DateMapper.asMilliseconds(feed.getPublishedDate),
    image          = podcastImageWithItunesFallback(feed),
    lastBuildDate  = None,  // TODO the parser does not yet produce this
    language       = Option(feed.getLanguage),
    generator      = Option(feed.getGenerator),
    copyright      = Option(feed.getCopyright),
    docs           = Option(feed.getDocs),
    managingEditor = Option(feed.getManagingEditor),
    webMaster      = Option(feed.getWebMaster),
    registration = PodcastRegistration(
      timestamp = None,
      complete  = None,
    ),
    atom         = podcastAtom(feed),
    persona      = podcastPersona(feed),
    itunes       = podcastItunes(feed),
    feedpress    = podcastFeedpress(feed),
    fyyd         = podcastFyyd(feed),
  )

  private def extractEpisodes(feed: SyndFeed, podcast: Podcast): List[Episode] = feed
    .getEntries
    .asScala
    .map(e => extractEpisode(e, podcast))
    .toList

  private def extractEpisode(entry: SyndEntry, podcast: Podcast): Episode = Episode(
    id              = None,
    podcastId       = None,
    podcastTitle    = podcast.title,
    title           = Option(entry.getTitle),
    link            = UrlMapper.sanitize(entry.getLink),
    pubDate         = DateMapper.asMilliseconds(entry.getPublishedDate),
    guid            = Option(entry.getUri),
    guidIsPermalink = None, // TODO welches feld von ROME korrespondiert zu diesem Boolean?
    description     = Option(entry.getDescription).map(_.getValue),
    image           = episodeImage(entry, podcast),
    contentEncoded  = episodeContentEncoded(entry),
    chapters        = episodeChapters(entry),
    atom            = episodeAtom(entry),
    persona         = episodePersona(entry),
    itunes          = episodeItunes(entry),
    enclosure       = episodeEnclosure(entry),
    registration = EpisodeRegistration(
      timestamp = None,
    )
  )

  private def podcastTitleWithImageFallback(feed: SyndFeed): Option[String] =
    Option(feed.getTitle)
      .orElse {
        Option(feed.getImage)
          .map(_.getTitle)
      }

  private def podcastLinkWithImageFallback(feed: SyndFeed): Option[String] =
    Option(feed.getLink)
      .orElse {
        Option(feed.getImage)
          .map(_.getLink)
      }
      .flatMap(UrlMapper.sanitize)

  private def podcastDescriptionWithImageFallback(feed: SyndFeed): Option[String] =
    Option(feed.getDescription)
      .orElse {
        Option(feed.getImage)
          .map(_.getDescription)
      }

  private def podcastImageWithItunesFallback(feed: SyndFeed): Option[String] = Option(feed.getImage)
    .filter(img => !isNullOrEmpty(img.getUrl))
    .map(_.getUrl)
    .orElse {
      RomeFeedExtractor
        .getItunesModule(feed)
        .asScala
        .map(_.getImage)
        .map(_.toString)
    }

  private def podcastItunes(feed: SyndFeed): PodcastItunes = RomeFeedExtractor
    .getItunesModule(feed)
    .asScala
    .map { itunes =>
      PodcastItunes(
        subtitle   = Option(itunes.getSubtitle),
        summary    = Option(itunes.getSummary),
        author     = Option(itunes.getAuthor),
        keywords   = Option(itunes.getKeywords)
          .map(_.toList)
          .getOrElse(Nil),
        categories = Option(itunes.getCategories)
          .map(_.asScala.map(_.getName))
          .map(_.toSet)  // eliminate duplicates
          .map(_.toList)
          .getOrElse(Nil),
        explicit   = Option(itunes.getExplicit),
        block      = Option(itunes.getBlock),
        typ        = Option(itunes.getType),
        owner      = podcastItunesOwner(itunes),
      )
    }.getOrElse(PodcastItunes())

  private def podcastItunesOwner(itunes: FeedInformation): Option[Person] = {
    val name = Option(itunes.getOwnerName)
    val email = Option(itunes.getOwnerEmailAddress)
    (name, email) match {
      case (None, None) => None
      case (_, _) => Some(Person(
        name  = name,
        email = email,
        uri   = None,
      ))
    }
  }

  private def podcastFeedpress(feed: SyndFeed): PodcastFeedpress = PodcastFeedpress(locale = None)

  private def podcastFyyd(feed: SyndFeed): PodcastFyyd = PodcastFyyd(verify = None)

  private def podcastAtom(feed: SyndFeed): Atom = Atom(
    links = podcastAtomLinks(feed),
  )

  private def podcastAtomLinks(feed: SyndFeed): List[AtomLink] =
    if (feed.getLinks.isEmpty) {
      RomeFeedExtractor
        .getAtomLinks(feed)
        .asScala
        .map(AtomLink.fromRome)
        .toList
    } else {
      feed
        .getLinks
        .asScala
        .map(AtomLink.fromRome)
        .toList
    }

  private def podcastPersona(feed: SyndFeed): Persona = Persona(
    authors      = podcastAuthors(feed),
    contributors = podcastContributors(feed),
  )

  private def podcastAuthors(feed: SyndFeed): List[Person] = feed
    .getAuthors
    .asScala
    .map(Person.fromRome)
    .toList

  private def podcastContributors(feed: SyndFeed): List[Person] = feed
    .getContributors
    .asScala
    .map(Person.fromRome)
    .toList

  private def episodeAtom(entry: SyndEntry): Atom = Atom(
    links = episodeAtomLinks(entry),
  )

  private def episodeAtomLinks(entry: SyndEntry): List[AtomLink] = RomeFeedExtractor
    .getAtomLinks(entry)
    .asScala
    .map(AtomLink.fromRome)
    .toList

  private def episodeImage(entry: SyndEntry, podcast: Podcast): Option[String] = RomeFeedExtractor
    .getItunesEntryInformation(entry)
    .asScala
    .flatMap(itunes => Option(itunes.getImage))
    //.map(_.getImage) // TODO why is this line not working, but the above?
    .map(_.toExternalForm)
    .orElse(podcast.image) // fallback is the podcast's image

  private def episodeItunes(entry: SyndEntry): EpisodeItunes = RomeFeedExtractor
    .getItunesEntryInformation(entry)
    .asScala
    .map { itunes =>
      EpisodeItunes(
        duration    = Option(itunes.getDuration).map(_.toString),
        subtitle    = Option(itunes.getSubtitle),
        author      = Option(itunes.getAuthor),
        summary     = Option(itunes.getSummary),
        season      = Option(itunes.getSeason),
        episode     = Option(itunes.getEpisode),
        episodeType = Option(itunes.getEpisodeType),
      )
    }.getOrElse(EpisodeItunes.empty)

  private def episodeEnclosure(entry: SyndEntry): EpisodeEnclosure = Option(entry.getEnclosures)
    .map { es =>
      if (es.size > 1) log.warn("Encountered multiple <enclosure> elements in <item> element")
      if (es.size > 0) {
        val e = entry.getEnclosures.get(0)
        EpisodeEnclosure(
          url    = Option(e.getUrl),
          length = Option(e.getLength),
          typ    = Option(e.getType),
        )
      } else {
        EpisodeEnclosure.empty
      }
    }.getOrElse(EpisodeEnclosure.empty)

  private def episodeContentEncoded(entry: SyndEntry): Option[String] = RomeFeedExtractor
    .getContentModule(entry)
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

  private def episodePersona(entry: SyndEntry): Persona = Persona(
    authors      = episodeAuthors(entry),
    contributors = episodeContributors(entry),
  )

  private def episodeAuthors(entry: SyndEntry): List[Person] = entry
    .getAuthors
    .asScala
    .map(Person.fromRome)
    .toList

  private def episodeContributors(entry: SyndEntry): List[Person] = entry
    .getContributors
    .asScala
    .map(Person.fromRome)
    .toList

  private def episodeChapters(entry: SyndEntry): List[Chapter] = RomeFeedExtractor
    .getPodloveSimpleChapterModule(entry)
    .asScala
    .map(_.getChapters)
    .map { _.asScala
      .map { c =>
        Chapter(
          start = Option(c.getStart),
          title = Option(c.getTitle),
          href  = Option(c.getHref),
          image = Option(c.getImage),
        )
      }.toList
    }.getOrElse(Nil)

}
