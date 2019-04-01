package hemin.engine.util.mapper

import com.google.common.base.Strings.isNullOrEmpty
import hemin.engine.model.{Episode, Document, IndexField, Podcast}
import hemin.engine.util.mapper.MapperErrors._
import org.apache.solr.common.SolrDocument

import scala.util.{Failure, Success, Try}

object IndexMapper {

  def toDocument(src: Podcast): Try[Document] = Option(src)
    .map { s =>
      Document(
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

  def toDocument(src: Episode): Try[Document] = Option(src)
    .map { s =>
      Document(
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

  def toDocument(src: SolrDocument): Try[Document] = Option(src)
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
                  case Success(p)  => toDocument(p)
                  case Failure(ex) => mapperFailurePodcastToIndexDoc(ex)
                }
              case "episode" =>
                EpisodeMapper.toEpisode(src) match {
                  case Success(e)  => toDocument(e): Try[Document]
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
