package io.disposia.engine.util.mapper

import io.disposia.engine.domain.episode.EpisodeItunesInfo
import io.disposia.engine.domain.{Episode, IndexDoc, IndexField}
import org.apache.solr.common.SolrDocument

object EpisodeMapper {

  /*
  @deprecated("do not use old DTOs anymore","0.1")
  def toEpisode(epsiode: OldEpisode): Episode = Option(epsiode)
    .map { e =>
      Episode(
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
    */


  def toEpisode(src: IndexDoc): Episode =
    Option(src)
      .map{ s =>
        Episode(
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

  def toEpisode(src: org.apache.lucene.document.Document): Episode =
    Option(src)
      .map { s =>
        Episode(
          id           = Option(s.get(IndexField.ID)),
          title        = Option(s.get(IndexField.TITLE)),
          podcastTitle = Option(s.get(IndexField.PODCAST_TITLE)),
          link         = Option(s.get(IndexField.LINK)),
          pubDate      = DateMapper.asLocalDateTime(s.get(IndexField.PUB_DATE)),
          description  = Option(s.get(IndexField.DESCRIPTION)),
          image        = Option(s.get(IndexField.ITUNES_IMAGE)),
          itunes = EpisodeItunesInfo(
            author   = Option(s.get(IndexField.ITUNES_AUTHOR)),
            summary  = Option(s.get(IndexField.ITUNES_SUMMARY)),
            duration = Option(s.get(IndexField.ITUNES_DURATION)),
          )
        )
      }.orNull

  def toEpisode(src: SolrDocument): Episode =
    Option(src)
      .map { s =>
        Episode(
          id           = SolrMapper.firstStringMatch(s, IndexField.ID),
          title        = SolrMapper.firstStringMatch(s, IndexField.TITLE),
          podcastTitle = SolrMapper.firstStringMatch(s, IndexField.PODCAST_TITLE),
          link         = SolrMapper.firstStringMatch(s, IndexField.LINK),
          pubDate      = SolrMapper.firstDateMatch(s, IndexField.PUB_DATE).flatMap(x => DateMapper.asLocalDateTime(x)),
          description  = SolrMapper.firstStringMatch(s, IndexField.DESCRIPTION),
          image        = SolrMapper.firstStringMatch(s, IndexField.ITUNES_IMAGE),
          itunes = EpisodeItunesInfo(
            author   = SolrMapper.firstStringMatch(s, IndexField.ITUNES_AUTHOR),
            summary  = SolrMapper.firstStringMatch(s, IndexField.ITUNES_SUMMARY),
            duration = SolrMapper.firstStringMatch(s, IndexField.ITUNES_DURATION),
          )
        )
      }.orNull

}
