package io.disposia.engine.util.mapper

import java.util
import java.util.List

import io.disposia.engine.domain.{Episode, IndexDoc, IndexField, Podcast}
import io.disposia.engine.mapper.IndexMapper
import org.apache.lucene.document.{Field, StringField, TextField}
import org.apache.solr.common.{SolrDocument, SolrInputDocument}

import scala.collection.JavaConverters._

object SolrMapper {

  private val dateMapper = io.disposia.engine.mapper.DateMapper.INSTANCE
  private val indexMapper = IndexMapper.INSTANCE

  def toLucene(src: IndexDoc): org.apache.lucene.document.Document =
    Option(src)
      .map { s =>
        val d = new org.apache.lucene.document.Document
        Option(s.getDocType).foreach        { x => d.add(new StringField(IndexField.DOC_TYPE, x, Field.Store.YES)) }
        Option(s.getId).foreach             { x => d.add(new StringField(IndexField.ID, x, Field.Store.YES)) }
        Option(s.getExo).foreach            { x => d.add(new StringField(IndexField.EXO, x, Field.Store.YES)) }
        Option(s.getTitle).foreach          { x => d.add(new TextField(IndexField.TITLE, x, Field.Store.YES)) }
        Option(s.getLink).foreach           { x => d.add(new TextField(IndexField.LINK, x, Field.Store.YES)) }
        Option(s.getDescription).foreach    { x => d.add(new TextField(IndexField.DESCRIPTION, x, Field.Store.YES)) }
        Option(s.getPodcastTitle).foreach   { x => d.add(new TextField(IndexField.PODCAST_TITLE, x, Field.Store.YES)) }
        Option(s.getPubDate).foreach        { x => d.add(new StringField(IndexField.PUB_DATE, dateMapper.asString(x), Field.Store.YES)) }
        Option(s.getImage).foreach          { x => d.add(new TextField(IndexField.ITUNES_IMAGE, x, Field.Store.YES)) }
        Option(s.getItunesAuthor).foreach   { x => d.add(new TextField(IndexField.ITUNES_AUTHOR, x, Field.Store.NO)) }
        Option(s.getItunesSummary).foreach  { x => d.add(new TextField(IndexField.ITUNES_SUMMARY, x, Field.Store.YES)) }
        Option(s.getChapterMarks).foreach   { x => d.add(new TextField(IndexField.CHAPTER_MARKS, x, Field.Store.NO)) }
        Option(s.getContentEncoded).foreach { x => d.add(new TextField(IndexField.CONTENT_ENCODED, x, Field.Store.NO)) }
        Option(s.getTranscript).foreach     { x => d.add(new TextField(IndexField.TRANSCRIPT, x, Field.Store.NO)) }
        Option(s.getWebsiteData).foreach    { x => d.add(new TextField(IndexField.WEBSITE_DATA, x, Field.Store.NO)) }
        d
      }
      .orNull

  def toSolr(src: Podcast): SolrInputDocument = toSolr(indexMapper.toImmutable(src))

  def toSolr(src: Episode): SolrInputDocument = toSolr(indexMapper.toImmutable(src))

  def toSolr(src: IndexDoc): SolrInputDocument =
    Option(src)
      .map { s =>
        val d = new SolrInputDocument
        Option(s.getDocType).foreach        { x => d.addField(IndexField.DOC_TYPE, x) }
        Option(s.getId).foreach             { x => d.addField(IndexField.ID, x) }
        Option(s.getExo).foreach            { x => d.addField(IndexField.EXO, x) }
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

  def firstOrNull(doc: SolrDocument, fieldName: String): String = doc
    .getFieldValue(fieldName)
    .asInstanceOf[util.List[String]]
    .asScala
    .headOption
    .orNull

}
