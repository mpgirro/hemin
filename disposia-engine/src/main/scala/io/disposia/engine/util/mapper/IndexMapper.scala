package io.disposia.engine.util.mapper

import com.google.common.base.Strings.isNullOrEmpty
import io.disposia.engine.domain.{Episode, IndexDoc, IndexField, Podcast}
import io.disposia.engine.mapper.SolrFieldMapper
import org.apache.solr.common.SolrDocument

object IndexMapper {

  /*
  def toIndexDoc(src: IndexDoc): OldIndexDoc = {
    Option(src)
      .map { s =>
        val b = ImmutableOldIndexDoc.builder()
        b.setDocType(s.docType.orNull)
        b.setId(s.id.orNull)
        b.setTitle(s.title.orNull)
        b.setLink(s.link.orNull)
        b.setDescription(s.description.orNull)
        b.setPubDate(s.pubDate.orNull)
        b.setImage(s.image.orNull)
        b.setItunesAuthor(s.itunesAuthor.orNull)
        b.setItunesSummary(s.itunesSummary.orNull)
        b.setPodcastTitle(s.podcastTitle.orNull)
        b.setChapterMarks(s.chapterMarks.orNull)
        b.setContentEncoded(s.contentEncoded.orNull)
        b.setTranscript(s.transcript.orNull)
        b.setWebsiteData(s.websiteData.orNull)
        b.create()
      }.orNull
  }
  */

  /*
  @deprecated("do not use old DTOs anymore","0.1")
  def toIndexDoc(src: OldIndexDoc): IndexDoc = {
    Option(src)
      .map { s =>
        IndexDoc(
          docType = Option(s.getDocType),
          id = Option(s.getId),
          title = Option(s.getTitle),
          link = Option(s.getLink),
          description = Option(s.getDescription),
          pubDate =Option(s.getPubDate),
          image = Option(s.getImage),
          itunesAuthor = Option(s.getItunesAuthor),
          itunesSummary = Option(s.getItunesSummary),
          podcastTitle = Option(s.getPodcastTitle),
          chapterMarks = Option(s.getChapterMarks),
          contentEncoded = Option(s.getContentEncoded),
          transcript = Option(s.getTranscript),
          websiteData = Option(s.getWebsiteData)
        )
      }.orNull
  }
  */

  //def toIndexDoc(is: java.util.List[OldIndexDoc]): List[IndexDoc] = is.asScala.map(i => toIndexDoc(i)).toList

  /*
  @deprecated("do not use old DTOs anymore","0.1")
  def toResults(src: OldResultWrapper): ResultsWrapper = {
    Option(src)
        .map { s =>
          ResultsWrapper(
            currPage  = s.getCurrPage,
            maxPage   = s.getMaxPage,
            totalHits = s.getTotalHits,
            results   = toIndexDoc(s.getResults)
          )
        }.orNull
  }
  */

  /*
  @deprecated("do not use old DTOs anymore","0.1")
  def toIndexDoc(src: OldPodcast): IndexDoc =
    Option(src)
      .map { s =>
        IndexDoc(
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
      */

  /*
  @deprecated("do not use old DTOs anymore","0.1")
  def toIndexDoc(src: OldEpisode): IndexDoc =
    Option(src)
      .map { s =>
        IndexDoc(
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
      */

  def toIndexDoc(src: Podcast): IndexDoc =
    Option(src)
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

  def toIndexDoc(src: Episode): IndexDoc =
    Option(src)
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


  def toIndexDoc(src: org.apache.lucene.document.Document): IndexDoc =
    Option(src)
      .map { s =>
        val docType: String = s.get(IndexField.DOC_TYPE)

        if (isNullOrEmpty(docType))
          throw new RuntimeException("Document type is required but found NULL")

        docType match {
          /* TODO delete
          case "podcast" => toIndexDoc(OldPodcastMapper.INSTANCE.toImmutable(s))
          case "episode" => toIndexDoc(OldEpisodeMapper.INSTANCE.toImmutable(s))
          */
          case "podcast" => toIndexDoc(PodcastMapper.toPodcast(src))
          case "episode" => toIndexDoc(EpisodeMapper.toEpisode(src))
          case _         => throw new RuntimeException("Unsupported document type : " + docType)
        }
      }
      .orNull

  def toIndexDoc(src: SolrDocument): IndexDoc =
    Option(src)
      .map { s=>
        val docType: String = SolrFieldMapper.INSTANCE.firstStringOrNull(s, IndexField.DOC_TYPE)

        if (isNullOrEmpty(docType))
          throw new RuntimeException("Document type is required but found NULL")

        docType match {
          /* TODO delete
          case "podcast" => toIndexDoc(OldPodcastMapper.INSTANCE.toImmutable(s))
          case "episode" => toIndexDoc(OldEpisodeMapper.INSTANCE.toImmutable(s))
          */
          case "podcast" => toIndexDoc(PodcastMapper.toPodcast(src))
          case "episode" => toIndexDoc(EpisodeMapper.toEpisode(src))
          case _         => throw new RuntimeException("Unsupported document type : " + docType)
        }
      }
      .orNull

}
