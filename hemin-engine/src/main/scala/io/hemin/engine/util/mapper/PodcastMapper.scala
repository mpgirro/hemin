package io.hemin.engine.util.mapper

import io.hemin.engine.model.info.PodcastItunesInfo
import io.hemin.engine.model.{IndexDoc, Podcast}
import io.hemin.engine.util.IndexField
import org.apache.solr.common.SolrDocument

object PodcastMapper {

  def toPodcast(src: IndexDoc): Podcast = Option(src)
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

  def toPodcast(src: org.apache.lucene.document.Document): Podcast = Option(src)
    .map { s =>
      Podcast(
        id          = LuceneMapper.get(s, IndexField.Id.entryName),
        title       = LuceneMapper.get(s, IndexField.Title.entryName),
        link        = LuceneMapper.get(s, IndexField.Link.entryName),
        pubDate     = DateMapper.asLocalDateTime(s.get(IndexField.PubDate.entryName)),
        description = LuceneMapper.get(s, IndexField.Description.entryName),
        image       = LuceneMapper.get(s, IndexField.ItunesImage.entryName),
      )
    }.orNull


  def toPodcast(src: SolrDocument): Podcast = Option(src)
    .map { s =>
      Podcast(
        id          = SolrMapper.firstStringMatch(s, IndexField.Id.entryName),
        title       = SolrMapper.firstStringMatch(s, IndexField.Title.entryName),
        link        = SolrMapper.firstStringMatch(s, IndexField.Link.entryName),
        pubDate     = SolrMapper.firstDateMatch(s, IndexField.PubDate.entryName).flatMap(x => DateMapper.asLocalDateTime(x)),
        description = SolrMapper.firstStringMatch(s, IndexField.Description.entryName),
        image       = SolrMapper.firstStringMatch(s, IndexField.ItunesImage.entryName),
      )
    }.orNull

}
