package io.hemin.engine.util.mapper

import io.hemin.engine.model.info.PodcastItunesInfo
import io.hemin.engine.model.{IndexDoc, Podcast}
import io.hemin.engine.model.IndexField
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
        id          = LuceneMapper.get(s, IndexField.ID),
        title       = LuceneMapper.get(s, IndexField.TITLE),
        link        = LuceneMapper.get(s, IndexField.LINK),
        pubDate     = DateMapper.asLocalDateTime(s.get(IndexField.PUB_DATE)),
        description = LuceneMapper.get(s, IndexField.DESCRIPTION),
        image       = LuceneMapper.get(s, IndexField.ITUNES_IMAGE),
      )
    }.orNull


  def toPodcast(src: SolrDocument): Podcast = Option(src)
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
