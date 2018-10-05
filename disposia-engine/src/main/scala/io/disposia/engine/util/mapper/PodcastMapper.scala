package io.disposia.engine.util.mapper

import io.disposia.engine.domain.podcast._
import io.disposia.engine.domain.{IndexDoc, IndexField, Podcast}
import org.apache.solr.common.SolrDocument

object PodcastMapper {

  /*
  @deprecated("do not use old DTOs anymore","0.1")
  def toPodcast(podcast: OldPodcast): Podcast = Option(podcast)
    .map { p =>
      Podcast(
        id          = Option(p.getId),
        title       = Option(p.getTitle),
        link        = Option(p.getLink),
        description = Option(p.getDescription),
        pubDate     = Option(p.getPubDate),
        image       = Option(p.getImage),
        meta = PodcastMetadata(
          lastBuildDate  = Option(p.getLastBuildDate),
          language       = Option(p.getLanguage),
          generator      = Option(p.getGenerator),
          copyright      = Option(p.getCopyright),
          docs           = Option(p.getDocs),
          managingEditor = Option(p.getManagingEditor),
        ),
        registration = PodcastRegistrationInfo(
          timestamp = Option(p.getRegistrationTimestamp),
          complete  = Option(p.getRegistrationComplete),
        ),
        itunes = PodcastItunesInfo(
          summary     = Option(p.getItunesSummary),
          author      = Option(p.getItunesAuthor),
          keywords    = Option(p.getItunesKeywords.split(", ")),
          categories  = Option(p.getItunesCategories.asScala.toSet),
          explicit    = Option(p.getItunesExplicit),
          block       = Option(p.getItunesBlock),
          podcastType = Option(p.getItunesType),
          ownerName   = Option(p.getItunesOwnerName),
          ownerEmail  = Option(p.getItunesOwnerEmail),
        ),
        feedpress = PodcastFeedpressInfo(
          locale = Option(p.getFeedpressLocale),
        ),
        fyyd = PodcastFyydInfo(
          verify = Option(p.getFyydVerify),
        )
      )
    }.orNull
    */

  def toPodcast(src: IndexDoc): Podcast =
    Option(src)
      .map{ s =>
        Podcast(
          id          = s.id,
          title       = s.title,
          link        = s.link,
          description = s.description,
          pubDate     = s.pubDate,
          image       = s.image,
          itunes = PodcastItunesInfo(
            author  = s.itunesAuthor,
            summary = s.itunesSummary
          )
        )
      }
      .orNull

  def toPodcast(src: org.apache.lucene.document.Document): Podcast =
    Option(src)
      .map { s =>
        Podcast(
          id          = Option(s.get(IndexField.ID)),
          title       = Option(s.get(IndexField.TITLE)),
          link        = Option(s.get(IndexField.LINK)),
          pubDate     = DateMapper.asLocalDateTime(s.get(IndexField.PUB_DATE)),
          description = Option(s.get(IndexField.DESCRIPTION)),
          image       = Option(s.get(IndexField.ITUNES_IMAGE)),
        )
      }.orNull

  def toPodcast(src: SolrDocument): Podcast =
    Option(src)
      .map { s =>
        Podcast(
          id          = SolrMapper.firstStringMatch(s, IndexField.ID),
          title       = SolrMapper.firstStringMatch(s, IndexField.TITLE),
          link        = SolrMapper.firstStringMatch(s, IndexField.LINK),
          pubDate     = SolrMapper.firstDateMatch(s, IndexField.PUB_DATE).flatMap(x => DateMapper.asLocalDateTime(x)),
          description = SolrMapper.firstStringMatch(s, IndexField.DESCRIPTION),
          image       = SolrMapper.firstStringMatch(s, IndexField.ITUNES_IMAGE),
        )
      }.orNull

}
