package io.hemin.engine.util.mapper

import com.google.common.base.Strings.isNullOrEmpty
import io.hemin.engine.model.{Episode, IndexDoc, IndexField, Podcast}
import io.hemin.engine.util.mapper.MapperErrors._
import org.apache.solr.common.SolrDocument

import scala.util.{Failure, Success, Try}

object IndexMapper {

  def toIndexDoc(src: Podcast): Try[IndexDoc] = Option(src)
    .map { s =>
      IndexDoc(
        docType        = Some("podcast"),
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

  def toIndexDoc(src: Episode): Try[IndexDoc] = Option(src)
    .map { s =>
      IndexDoc(
        docType        = Some("episode"),
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

  def toIndexDoc(doc: org.apache.lucene.document.Document): Try[IndexDoc] = Option(doc)
    .map { d =>
      val docType = d.get(IndexField.DocType.entryName)
      if (isNullOrEmpty(docType)) {
        mapperFailureUnsupportedIndexDocumentType("NULL")
      } else {
        docType match {
          case "podcast" =>
            PodcastMapper.toPodcast(doc) match {
              case Success(p)  => toIndexDoc(p)
              case Failure(ex) => mapperFailurePodcastToIndexDoc(ex)
            }
          case "episode" =>
            EpisodeMapper.toEpisode(doc) match {
              case Success(e)  => toIndexDoc(e)
              case Failure(ex) => mapperFailureEpisodeToIndexDoc(ex)
            }
          case _ => mapperFailureUnsupportedIndexDocumentType(docType)
        }
      }
    }
    .getOrElse(mapperFailureLuceneToIndexDoc(doc))

  def toIndexDoc(src: SolrDocument): Try[IndexDoc] = Option(src)
    .map { s =>
      val docType = SolrMapper.firstStringMatch(s, IndexField.DocType.entryName)
      docType match {
        case Some(dt) =>
          if (isNullOrEmpty(dt)) {
            mapperFailureUnsupportedIndexDocumentType(dt)
          } else {
            dt match {
              case "podcast" =>
                PodcastMapper.toPodcast(src) match {
                  case Success(p)  => toIndexDoc(p)
                  case Failure(ex) => mapperFailurePodcastToIndexDoc(ex)
                }
              case "episode" =>
                EpisodeMapper.toEpisode(src) match {
                  case Success(e)  => toIndexDoc(e): Try[IndexDoc]
                  case Failure(ex) => mapperFailureEpisodeToIndexDoc(ex)
                }
              case _ => mapperFailureUnsupportedIndexDocumentType(dt)
            }
          }
        case None => mapperFailureIndexFieldNotPresent(IndexField.DocType.entryName)
      }
    }
    .getOrElse(mapperFailureSolrToIndexDoc(src))

}
