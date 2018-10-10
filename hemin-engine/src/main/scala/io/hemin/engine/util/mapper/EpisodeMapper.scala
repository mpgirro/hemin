package io.hemin.engine.util.mapper

import io.hemin.engine.domain.info.EpisodeItunesInfo
import io.hemin.engine.domain.{Episode, IndexDoc}
import io.hemin.engine.domain.IndexField
import org.apache.solr.common.SolrDocument

object EpisodeMapper {

  def toEpisode(src: IndexDoc): Episode = Option(src)
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


  def toEpisode(src: org.apache.lucene.document.Document): Episode = Option(src)
    .map { s =>
      Episode(
        id           = LuceneMapper.get(s, IndexField.ID),
        title        = LuceneMapper.get(s, IndexField.TITLE),
        podcastTitle = LuceneMapper.get(s, IndexField.PODCAST_TITLE),
        link         = LuceneMapper.get(s, IndexField.LINK),
        pubDate      = DateMapper.asLocalDateTime(s.get(IndexField.PUB_DATE)),
        description  = LuceneMapper.get(s, IndexField.DESCRIPTION),
        image        = LuceneMapper.get(s, IndexField.ITUNES_IMAGE),
        itunes = EpisodeItunesInfo(
          author   = LuceneMapper.get(s, IndexField.ITUNES_AUTHOR),
          summary  = LuceneMapper.get(s, IndexField.ITUNES_SUMMARY),
          duration = LuceneMapper.get(s, IndexField.ITUNES_DURATION),
        )
      )
    }.orNull


  def toEpisode(src: SolrDocument): Episode = Option(src)
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
