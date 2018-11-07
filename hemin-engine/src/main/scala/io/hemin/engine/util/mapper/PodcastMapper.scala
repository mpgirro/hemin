package io.hemin.engine.util.mapper

import io.hemin.engine.model.info.PodcastItunesInfo
import io.hemin.engine.model.{IndexDoc, IndexField, Podcast}
import io.hemin.engine.util.mapper.MapperErrors._
import org.apache.solr.common.SolrDocument

import scala.util.{Success, Try}

object PodcastMapper {

  def toPodcast(doc: IndexDoc): Try[Podcast] = Option(doc)
    .map{ d =>
      Podcast(
        id          = d.id,
        title       = d.title,
        link        = d.link,
        description = d.description,
        pubDate     = d.pubDate,
        image       = d.image,
        itunes = PodcastItunesInfo(
          author  = d.itunesAuthor,
          summary = d.itunesSummary
        )
      )
    }
    .map(Success(_))
    .getOrElse(mapperFailureIndexToPodcast(doc))

  def toPodcast(doc: org.apache.lucene.document.Document): Try[Podcast] = Option(doc)
    .map { d =>
      Podcast(
        id          = LuceneMapper.get(d, IndexField.Id.entryName),
        title       = LuceneMapper.get(d, IndexField.Title.entryName),
        link        = LuceneMapper.get(d, IndexField.Link.entryName),
        pubDate     = DateMapper.asLocalDateTime(d.get(IndexField.PubDate.entryName)),
        description = LuceneMapper.get(d, IndexField.Description.entryName),
        image       = LuceneMapper.get(d, IndexField.ItunesImage.entryName),
      )
    }
    .map(Success(_))
    .getOrElse(mapperFailureLuceneToPodcast(doc))


  def toPodcast(doc: SolrDocument): Try[Podcast] = Option(doc)
    .map { d =>
      Podcast(
        id          = SolrMapper.firstStringMatch(d, IndexField.Id.entryName),
        title       = SolrMapper.firstStringMatch(d, IndexField.Title.entryName),
        link        = SolrMapper.firstStringMatch(d, IndexField.Link.entryName),
        pubDate     = SolrMapper.firstDateMatch(d, IndexField.PubDate.entryName).flatMap(x => DateMapper.asLocalDateTime(x)),
        description = SolrMapper.firstStringMatch(d, IndexField.Description.entryName),
        image       = SolrMapper.firstStringMatch(d, IndexField.ItunesImage.entryName),
      )
    }
    .map(Success(_))
    .getOrElse(mapperFailureSolrToPodcast(doc))

}
