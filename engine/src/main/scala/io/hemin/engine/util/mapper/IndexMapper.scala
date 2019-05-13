package io.hemin.engine.util.mapper

import io.hemin.engine.model.{Document, Episode, IndexField, Podcast}
import io.hemin.engine.util.mapper.MapperErrors._
import org.apache.solr.common.SolrDocument

import scala.util.{Failure, Success, Try}

object IndexMapper {

  final val podcastDocType: String = "podcast"
  final val episodeDocType: String = "episode"

  def toDocument(src: Podcast): Try[Document] = Option(src)
    .map { s =>
      Document(
        docType        = Some(podcastDocType),
        id             = s.id,
        title          = s.title,
        link           = s.link,
        description    = s.description,
        pubDate        = s.pubDate,
        image          = s.image,
        itunesAuthor   = s.itunes.author,
        itunesSummary  = s.itunes.summary,
        podcastTitle   = None,
        chapterMarks   = None,
        contentEncoded = None,
        transcript     = None,
        websiteData    = None
      )
    }
    .map(Success(_))
    .getOrElse(mapperFailurePodcastToIndexDoc(src))

  def toDocument(src: Episode): Try[Document] = Option(src)
    .map { s =>
      Document(
        docType        = Some(episodeDocType),
        id             = s.id,
        title          = s.title,
        link           = s.link,
        description    = s.description,
        pubDate        = s.pubDate,
        image          = s.image,
        itunesAuthor   = s.itunes.author,
        itunesSummary  = s.itunes.summary,
        podcastTitle   = s.podcastTitle,
        chapterMarks   = Some(s.chapters.mkString("\n")),
        contentEncoded = s.contentEncoded,
        transcript     = None,
        websiteData    = None
      )
    }
    .map(Success(_))
    .getOrElse(mapperFailureEpisodeToIndexDoc(src))

  def toDocument(src: SolrDocument): Try[Document] = Option(src)
    .map { s =>
      val docType = SolrMapper.firstStringMatch(s, IndexField.DocType.entryName)
      docType match {
        case Some(IndexMapper.podcastDocType) =>
          PodcastMapper.toPodcast(src) match {
            case Success(p)  => toDocument(p)
            case Failure(ex) => mapperFailurePodcastToIndexDoc(ex)
          }
        case Some(IndexMapper.episodeDocType) =>
          EpisodeMapper.toEpisode(src) match {
            case Success(e)  => toDocument(e): Try[Document]
            case Failure(ex) => mapperFailureEpisodeToIndexDoc(ex)
          }
        case Some(dt) => mapperFailureUnsupportedIndexDocumentType(dt)
        case None     => mapperFailureIndexFieldNotPresent(IndexField.DocType.entryName)
      }
    }
    .getOrElse(mapperFailureSolrToIndexDoc(src))

}
