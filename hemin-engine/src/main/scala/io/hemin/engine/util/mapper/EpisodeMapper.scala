package io.hemin.engine.util.mapper

import io.hemin.engine.model.info.EpisodeItunesInfo
import io.hemin.engine.model.{Episode, IndexDoc, IndexField}
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
        id           = LuceneMapper.get(s, IndexField.Id.entryName),
        title        = LuceneMapper.get(s, IndexField.Title.entryName),
        podcastTitle = LuceneMapper.get(s, IndexField.PodcastTitle.entryName),
        link         = LuceneMapper.get(s, IndexField.Link.entryName),
        pubDate      = DateMapper.asLocalDateTime(s.get(IndexField.PubDate.entryName)),
        description  = LuceneMapper.get(s, IndexField.Description.entryName),
        image        = LuceneMapper.get(s, IndexField.ItunesImage.entryName),
        itunes = EpisodeItunesInfo(
          author   = LuceneMapper.get(s, IndexField.ItunesAuthor.entryName),
          summary  = LuceneMapper.get(s, IndexField.ItunesSummary.entryName),
          duration = LuceneMapper.get(s, IndexField.ItunesDuration.entryName),
        )
      )
    }.orNull

  def toEpisode(src: SolrDocument): Episode = Option(src)
    .map { s =>
      Episode(
        id           = SolrMapper.firstStringMatch(s, IndexField.Id.entryName),
        title        = SolrMapper.firstStringMatch(s, IndexField.Title.entryName),
        podcastTitle = SolrMapper.firstStringMatch(s, IndexField.PodcastTitle.entryName),
        link         = SolrMapper.firstStringMatch(s, IndexField.Link.entryName),
        pubDate      = SolrMapper.firstDateMatch(s, IndexField.PubDate.entryName).flatMap(x => DateMapper.asLocalDateTime(x)),
        description  = SolrMapper.firstStringMatch(s, IndexField.Description.entryName),
        image        = SolrMapper.firstStringMatch(s, IndexField.ItunesImage.entryName),
        itunes = EpisodeItunesInfo(
          author   = SolrMapper.firstStringMatch(s, IndexField.ItunesAuthor.entryName),
          summary  = SolrMapper.firstStringMatch(s, IndexField.ItunesSummary.entryName),
          duration = SolrMapper.firstStringMatch(s, IndexField.ItunesDuration.entryName),
        )
      )
    }.orNull

}
