package io.disposia.engine.util.mapper

import java.util.Date

import com.google.common.collect.Lists
import io.disposia.engine.domain.{IndexDoc, IndexField}
import org.apache.solr.common.{SolrDocument, SolrInputDocument}

object SolrMapper {

  def toSolr(src: IndexDoc): SolrInputDocument = Option(src)
    .map { s =>
      val d = new SolrInputDocument
      s.docType.foreach        { x => d.addField(IndexField.DOC_TYPE, x) }
      s.id.foreach             { x => d.addField(IndexField.ID, x) }
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


  def firstMatch(doc: SolrDocument, fieldName: String): Option[Any] = {
    val os = doc.getFieldValues(fieldName)
    if (os == null || os.isEmpty) {
      None
    } else {
      Option(Lists.newArrayList(os).get(0))
    }
  }

  def firstStringMatch(doc: SolrDocument, fieldName: String): Option[String] =
    firstMatch(doc, fieldName).map(_.asInstanceOf[String])

  def firstDateMatch(doc: SolrDocument, fieldName: String): Option[Date] =
    firstMatch(doc, fieldName).map(_.asInstanceOf[Date])

}
