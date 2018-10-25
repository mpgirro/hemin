package io.hemin.engine.util.mapper

import com.google.common.base.Strings.isNullOrEmpty
import io.hemin.engine.model.{Episode, IndexDoc, Podcast}
import io.hemin.engine.model.IndexField
import org.apache.solr.common.SolrDocument

object IndexMapper {

  def toIndexDoc(src: Podcast): IndexDoc = Option(src)
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
    }.orNull


  def toIndexDoc(src: Episode): IndexDoc = Option(src)
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
    }.orNull



  def toIndexDoc(src: org.apache.lucene.document.Document): IndexDoc = Option(src)
    .map { s =>
      val docType = s.get(IndexField.DOC_TYPE)

      if (isNullOrEmpty(docType))
        throw new RuntimeException("Document type is required but found NULL")

      docType match {
        case "podcast" => toIndexDoc(PodcastMapper.toPodcast(src))
        case "episode" => toIndexDoc(EpisodeMapper.toEpisode(src))
        case _         => throw new RuntimeException("Unsupported document type : " + docType)
      }
    }
    .orNull


  def toIndexDoc(src: SolrDocument): IndexDoc = Option(src)
    .map { s =>
      val docType = SolrMapper.firstStringMatch(s, IndexField.DOC_TYPE)
      docType match {
        case Some(dt) =>
          if (isNullOrEmpty(dt))
            throw new RuntimeException("Document type is required but found NULL")

          dt match {
            case "podcast" => toIndexDoc(PodcastMapper.toPodcast(src))
            case "episode" => toIndexDoc(EpisodeMapper.toEpisode(src))
            case _         => throw new RuntimeException("Unsupported document type : " + docType)
          }
        case None => throw new RuntimeException("Field '" + IndexField.DOC_TYPE + " could not be extracted")
      }
    }.orNull


}
