package io.hemin.engine.util.mapper

import java.util.Date

import com.google.common.collect.Lists
import io.hemin.engine.model.{IndexDoc, IndexField}
import io.hemin.engine.util.mapper.MapperErrors._
import org.apache.solr.common.{SolrDocument, SolrInputDocument}

import scala.util.{Success, Try}

object SolrMapper {

  def toSolr(src: IndexDoc): Try[SolrInputDocument] = Option(src)
    .map { s =>
      val d = new SolrInputDocument
      s.docType.foreach        { x => d.addField(IndexField.DocType.entryName, x) }
      s.id.foreach             { x => d.addField(IndexField.Id.entryName, x) }
      s.title.foreach          { x => d.addField(IndexField.Title.entryName, x) }
      s.link.foreach           { x => d.addField(IndexField.Link.entryName, x) }
      s.description.foreach    { x => d.addField(IndexField.Description.entryName, x) }
      s.podcastTitle.foreach   { x => d.addField(IndexField.PodcastTitle.entryName, x) }
      s.pubDate.foreach        { x => d.addField(IndexField.PubDate.entryName, DateMapper.asString(x).get) }
      s.image.foreach          { x => d.addField(IndexField.ItunesImage.entryName, x) }
      s.itunesAuthor.foreach   { x => d.addField(IndexField.ItunesAuthor.entryName, x) }
      s.itunesSummary.foreach  { x => d.addField(IndexField.ItunesSummary.entryName, x) }
      s.chapterMarks.foreach   { x => d.addField(IndexField.ChapterMarks.entryName, x) }
      s.contentEncoded.foreach { x => d.addField(IndexField.ContentEncoded.entryName, x) }
      s.transcript.foreach     { x => d.addField(IndexField.Transcript.entryName, x) }
      s.websiteData.foreach    { x => d.addField(IndexField.WebsiteData.entryName, x) }
      d
    }
    .map(Success(_))
    .getOrElse(mapperFailureIndexToSolr(src))

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
