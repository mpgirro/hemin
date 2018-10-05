package io.disposia.engine.util.mapper

import io.disposia.engine.domain.IndexField
import io.disposia.engine.olddomain.OldPodcast
import io.disposia.engine.mapper.{DateMapper, SolrFieldMapper}
import io.disposia.engine.newdomain.podcast._
import io.disposia.engine.newdomain.{NewIndexDoc, NewPodcast}
import org.apache.solr.common.SolrDocument

import scala.collection.JavaConverters._

object NewPodcastMapper {

  @deprecated("do not use old DTOs anymore","0.1")
  def toPodcast(podcast: OldPodcast): NewPodcast = Option(podcast)
    .map { p =>
      NewPodcast(
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

  def toPodcast(src: NewIndexDoc): NewPodcast =
    Option(src)
      .map{ s =>
        NewPodcast(
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

  def toPodcast(src: org.apache.lucene.document.Document): NewPodcast =
    Option(src)
      .map { s =>
        NewPodcast(
          id = Option(s.get(IndexField.ID)),
          title = Option(s.get(IndexField.TITLE)),
          link = Option(s.get(IndexField.LINK)),
          pubDate = Option(DateMapper.INSTANCE
            .asLocalDateTime(s.get(IndexField.PUB_DATE))),
          description = Option(s.get(IndexField.DESCRIPTION)),
          image = Option(s.get(IndexField.ITUNES_IMAGE)),
        )
      }.orNull

  def toPodcast(src: SolrDocument): NewPodcast =
    Option(src)
      .map { s =>
        NewPodcast(
          id = Option(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.ID)),
          title = Option(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.TITLE)),
          link = Option(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.LINK)),
          pubDate = Option(DateMapper.INSTANCE
            .asLocalDateTime(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.PUB_DATE))),
          description = Option(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.DESCRIPTION)),
          image = Option(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.ITUNES_IMAGE)),
        )
      }.orNull

}
