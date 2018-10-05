package io.disposia.engine.util.mapper

import java.util
import java.util.Date

import io.disposia.engine.domain.{IndexDoc, IndexField}
import org.apache.solr.common.{SolrDocument, SolrInputDocument}

import scala.collection.JavaConverters._

object SolrMapper {

  /*
  private val dateMapper = io.disposia.engine.oldmapper.OldDateMapper.INSTANCE
  private val indexMapper = OldIndexMapper.INSTANCE
  */

  /*
  @deprecated("do not use old DTOs anymore","0.1")
  def toSolr(src: OldPodcast): SolrInputDocument = toSolr(indexMapper.toImmutable(src))

  @deprecated("do not use old DTOs anymore","0.1")
  def toSolr(src: OldEpisode): SolrInputDocument = toSolr(indexMapper.toImmutable(src))

  @deprecated("do not use old DTOs anymore","0.1")
  def toSolr(src: OldIndexDoc): SolrInputDocument =
    Option(src)
      .map { s =>
        val d = new SolrInputDocument
        Option(s.getDocType).foreach        { x => d.addField(IndexField.DOC_TYPE, x) }
        Option(s.getId).foreach             { x => d.addField(IndexField.ID, x) }
        //Option(s.getExo).foreach            { x => d.addField(IndexField.EXO, x) }
        Option(s.getTitle).foreach          { x => d.addField(IndexField.TITLE, x) }
        Option(s.getLink).foreach           { x => d.addField(IndexField.LINK, x) }
        Option(s.getDescription).foreach    { x => d.addField(IndexField.DESCRIPTION, x) }
        Option(s.getPodcastTitle).foreach   { x => d.addField(IndexField.PODCAST_TITLE, x) }
        Option(s.getPubDate).foreach        { x => d.addField(IndexField.PUB_DATE, dateMapper.asString(x)) }
        Option(s.getImage).foreach          { x => d.addField(IndexField.ITUNES_IMAGE, x) }
        Option(s.getItunesAuthor).foreach   { x => d.addField(IndexField.ITUNES_AUTHOR, x) }
        Option(s.getItunesSummary).foreach  { x => d.addField(IndexField.ITUNES_SUMMARY, x) }
        Option(s.getChapterMarks).foreach   { x => d.addField(IndexField.CHAPTER_MARKS, x) }
        Option(s.getContentEncoded).foreach { x => d.addField(IndexField.CONTENT_ENCODED, x) }
        Option(s.getTranscript).foreach     { x => d.addField(IndexField.TRANSCRIPT, x) }
        Option(s.getWebsiteData).foreach    { x => d.addField(IndexField.WEBSITE_DATA, x) }
        d
      }
      .orNull
      */


  def toSolr(src: IndexDoc): SolrInputDocument =
    Option(src)
      .map { s =>
        val d = new SolrInputDocument
        s.docType.foreach        { x => d.addField(IndexField.DOC_TYPE, x) }
        s.id.foreach             { x => d.addField(IndexField.ID, x) }
        //Option(s.getExo).foreach            { x => d.addField(IndexField.EXO, x) }
        s.title.foreach          { x => d.addField(IndexField.TITLE, x) }
        s.link.foreach           { x => d.addField(IndexField.LINK, x) }
        s.description.foreach    { x => d.addField(IndexField.DESCRIPTION, x) }
        s.podcastTitle.foreach   { x => d.addField(IndexField.PODCAST_TITLE, x) }
        s.pubDate.foreach        { x => d.addField(IndexField.PUB_DATE, DateMapper.asString(x).get) }
        s.image.foreach          { x => d.addField(IndexField.ITUNES_IMAGE, x) }
        s.itunesAuthor.foreach   { x => d.addField(IndexField.ITUNES_AUTHOR, x) }
        s.itunesSummary.foreach  { x => d.addField(IndexField.ITUNES_SUMMARY, x) }
        s.chapterMarks.foreach   { x => d.addField(IndexField.CHAPTER_MARKS, x) }
        s.contentEncoded.foreach { x => d.addField(IndexField.CONTENT_ENCODED, x) }
        s.transcript.foreach     { x => d.addField(IndexField.TRANSCRIPT, x) }
        s.websiteData.foreach    { x => d.addField(IndexField.WEBSITE_DATA, x) }
        d
      }
      .orNull

  def firstStringMatch(doc: SolrDocument, fieldName: String): Option[String] = doc
    .getFieldValue(fieldName)
    .asInstanceOf[util.List[String]]
    .asScala
    .headOption

  def firstDateMatch(doc: SolrDocument, fieldName: String): Option[Date] =
    firstStringMatch(doc, fieldName).map(_.asInstanceOf[Date])

}
