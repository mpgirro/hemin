package io.disposia.engine.util.mapper

import io.disposia.engine.domain.IndexField
import io.disposia.engine.mapper.{DateMapper, SolrFieldMapper}
import io.disposia.engine.newdomain.episode.{EpisodeEnclosureInfo, EpisodeItunesInfo, EpisodeRegistrationInfo}
import io.disposia.engine.newdomain.{NewEpisode, NewIndexDoc}
import io.disposia.engine.olddomain.OldEpisode
import org.apache.solr.common.SolrDocument

object NewEpisodeMapper {

  @deprecated("do not use old DTOs anymore","0.1")
  def toEpisode(epsiode: OldEpisode): NewEpisode = Option(epsiode)
    .map { e =>
      NewEpisode(
        id              = Option(e.getId),
        title           = Option(e.getTitle),
        podcastId       = Option(e.getPodcastId),
        podcastTitle    = Option(e.getPodcastTitle),
        link            = Option(e.getLink),
        pubDate         = Option(e.getPubDate),
        guid            = Option(e.getGuid),
        guidIsPermalink = Option(e.getGuidIsPermaLink),
        description     = Option(e.getDescription),
        image           = Option(e.getImage),
        contentEncoded  = Option(e.getContentEncoded),
        itunes = EpisodeItunesInfo(
          duration    = Option(e.getItunesDuration),
          subtitle    = Option(e.getItunesSubtitle),
          author      = Option(e.getItunesAuthor),
          summary     = Option(e.getItunesSummary),
          season      = Option(e.getItunesSeason),
          episode     = Option(e.getItunesEpisode),
          episodeType = Option(e.getItunesEpisodeType),
        ),
        enclosure = EpisodeEnclosureInfo(
          url    = Option(e.getEnclosureUrl),
          length = Option(e.getEnclosureLength),
          typ    = Option(e.getEnclosureType),
        ),
        registration = EpisodeRegistrationInfo(
          timestamp = Option(e.getRegistrationTimestamp),
        )
      )
    }.orNull


  def toEpisode(src: NewIndexDoc): NewEpisode =
    Option(src)
      .map{ s =>
        NewEpisode(
          id          = s.id,
          title       = s.title,
          link        = s.link,
          description = s.description,
          pubDate     = s.pubDate,
          image       = s.image,
          itunes = EpisodeItunesInfo(
            author  = s.itunesAuthor,
            summary = s.itunesSummary,
            //duration = s.itunesDuration,
          )
        )
      }
      .orNull

  def toEpisode(src: org.apache.lucene.document.Document): NewEpisode =
    Option(src)
      .map { s =>
        NewEpisode(
          id           = Option(s.get(IndexField.ID)),
          title        = Option(s.get(IndexField.TITLE)),
          podcastTitle = Option(s.get(IndexField.PODCAST_TITLE)),
          link         = Option(s.get(IndexField.LINK)),
          pubDate      = Option(DateMapper.INSTANCE
            .asLocalDateTime(s.get(IndexField.PUB_DATE))),
          description  = Option(s.get(IndexField.DESCRIPTION)),
          image        = Option(s.get(IndexField.ITUNES_IMAGE)),
          itunes = EpisodeItunesInfo(
            author   = Option(s.get(IndexField.ITUNES_AUTHOR)),
            summary  = Option(s.get(IndexField.ITUNES_SUMMARY)),
            duration = Option(s.get(IndexField.ITUNES_DURATION)),
          )
        )
      }.orNull

  def toEpisode(src: SolrDocument): NewEpisode =
    Option(src)
      .map { s =>
        NewEpisode(
          id           = Option(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.ID)),
          title        = Option(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.TITLE)),
          podcastTitle = Option(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.PODCAST_TITLE)),
          link         = Option(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.LINK)),
          pubDate      = Option(DateMapper.INSTANCE
            .asLocalDateTime(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.PUB_DATE))),
          description  = Option(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.DESCRIPTION)),
          image        = Option(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.ITUNES_IMAGE)),
          itunes = EpisodeItunesInfo(
            author   = Option(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.ITUNES_AUTHOR)),
            summary  = Option(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.ITUNES_SUMMARY)),
            duration = Option(SolrFieldMapper.INSTANCE.stringOrNull(s, IndexField.ITUNES_DURATION)),
          )
        )
      }.orNull

}
