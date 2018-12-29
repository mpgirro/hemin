package io.hemin.engine.util.mapper

import io.hemin.engine.model.{IndexDoc, IndexField, Podcast, PodcastItunes}
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
        itunes = PodcastItunes(
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
        pubDate     = DateMapper.asZonedDateTime(d.get(IndexField.PubDate.entryName)),
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
        pubDate     = SolrMapper.firstDateMatch(d, IndexField.PubDate.entryName).flatMap(x => DateMapper.asZonedDateTime(x)),
        description = SolrMapper.firstStringMatch(d, IndexField.Description.entryName),
        image       = SolrMapper.firstStringMatch(d, IndexField.ItunesImage.entryName),
      )
    }
    .map(Success(_))
    .getOrElse(mapperFailureSolrToPodcast(doc))

  def toTeaser(podcast: Podcast): Option[Podcast] = Option(podcast)
    .map { p =>
      Podcast(
        id = p.id,
        title = p.title,
        link = p.link,
        description = p.description,
        pubDate = p.pubDate,
        image = p.image,
        itunes = PodcastItunes(
          summary = p.itunes.summary,
        )
      )
    }

}
