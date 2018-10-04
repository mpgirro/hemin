package io.disposia.engine.util.mapper

import com.google.common.base.Strings.isNullOrEmpty
import io.disposia.engine.domain.{Episode, IndexField, Podcast}
import io.disposia.engine.mapper.SolrFieldMapper
import io.disposia.engine.newdomain.{NewEpisode, NewIndexDoc, NewPodcast}
import org.apache.solr.common.SolrDocument

import scala.collection.JavaConverters._

object NewIndexMapper {

  @deprecated
  def toIndexDoc(src: Podcast): NewIndexDoc =
    Option(src)
      .map { s =>
        NewIndexDoc(
          docType = Some("podcast"),
          id = Option(s.getId),
          title = Option(s.getTitle),
          link = Option(s.getLink),
          description = Option(s.getDescription),
          pubDate =Option(s.getPubDate),
          image = Option(s.getImage),
          itunesAuthor = Option(s.getItunesAuthor),
          itunesSummary = Option(s.getItunesSummary),
          podcastTitle = None,
          chapterMarks = None,
          contentEncoded = None,
          transcript = None,
          websiteData = None
        )
      }
      .orNull

  @deprecated
  def toIndexDoc(src: Episode): NewIndexDoc =
    Option(src)
      .map { s =>
        NewIndexDoc(
          docType = Some("episode"),
          id = Option(s.getId),
          title = Option(s.getTitle),
          link = Option(s.getLink),
          description = Option(s.getDescription),
          pubDate = Option(s.getPubDate),
          image = Option(s.getImage),
          itunesAuthor = Option(s.getItunesAuthor),
          itunesSummary = Option(s.getItunesSummary),
          podcastTitle = Option(s.getPodcastTitle),
          chapterMarks = Option(s.getChapters.asScala.mkString("\n")),
          contentEncoded = Option(s.getContentEncoded),
          transcript = None,
          websiteData = None
        )
      }
      .orNull

  def toIndexDoc(src: NewPodcast): NewIndexDoc =
    Option(src)
    .map { s =>
      NewIndexDoc(
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

  def toIndexDoc(src: NewEpisode): NewIndexDoc =
    Option(src)
      .map { s =>
        NewIndexDoc(
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


  def toIndexDoc(src: org.apache.lucene.document.Document): NewIndexDoc =
    Option(src)
      .map { s =>
        val docType: String = s.get(IndexField.DOC_TYPE)

        if (isNullOrEmpty(docType))
          throw new RuntimeException("Document type is required but found NULL")

        docType match {
          /* TODO delete
          case "podcast" => toIndexDoc(PodcastMapper.INSTANCE.toImmutable(s))
          case "episode" => toIndexDoc(EpisodeMapper.INSTANCE.toImmutable(s))
          */
          case "podcast" => toIndexDoc(NewPodcastMapper.toPodcast(src))
          case "episode" => toIndexDoc(NewEpisodeMapper.toEpisode(src))
          case _         => throw new RuntimeException("Unsupported document type : " + docType)
        }
      }
      .orNull

  def toIndexDoc(src: SolrDocument): NewIndexDoc =
    Option(src)
      .map { s=>
        val docType: String = SolrFieldMapper.INSTANCE.firstStringOrNull(s, IndexField.DOC_TYPE)

        if (isNullOrEmpty(docType))
          throw new RuntimeException("Document type is required but found NULL")

        docType match {
          /* TODO delete
          case "podcast" => toIndexDoc(PodcastMapper.INSTANCE.toImmutable(s))
          case "episode" => toIndexDoc(EpisodeMapper.INSTANCE.toImmutable(s))
          */
          case "podcast" => toIndexDoc(NewPodcastMapper.toPodcast(src))
          case "episode" => toIndexDoc(NewEpisodeMapper.toEpisode(src))
          case _         => throw new RuntimeException("Unsupported document type : " + docType)
        }
      }
      .orNull

}
