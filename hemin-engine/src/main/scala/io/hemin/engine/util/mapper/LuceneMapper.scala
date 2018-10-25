package io.hemin.engine.util.mapper

import io.hemin.engine.model.IndexDoc
import io.hemin.engine.model.IndexField
import org.apache.lucene.document.{Field, StringField, TextField}


object LuceneMapper {

  def toLucene(src: IndexDoc): org.apache.lucene.document.Document = Option(src)
    .map { s =>
      val d = new org.apache.lucene.document.Document
      s.docType.foreach        { x => d.add(new StringField(IndexField.DOC_TYPE, x, Field.Store.YES)) }
      s.id.foreach             { x => d.add(new StringField(IndexField.ID, x, Field.Store.YES)) }
      s.title.foreach          { x => d.add(new TextField(IndexField.TITLE, x, Field.Store.YES)) }
      s.link.foreach           { x => d.add(new TextField(IndexField.LINK, x, Field.Store.YES)) }
      s.description.foreach    { x => d.add(new TextField(IndexField.DESCRIPTION, x, Field.Store.YES)) }
      s.podcastTitle.foreach   { x => d.add(new TextField(IndexField.PODCAST_TITLE, x, Field.Store.YES)) }
      s.pubDate.foreach        { x => d.add(new StringField(IndexField.PUB_DATE, DateMapper.asString(x).get, Field.Store.YES)) }
      s.image.foreach          { x => d.add(new TextField(IndexField.ITUNES_IMAGE, x, Field.Store.YES)) }
      s.itunesAuthor.foreach   { x => d.add(new TextField(IndexField.ITUNES_AUTHOR, x, Field.Store.NO)) }
      s.itunesSummary.foreach  { x => d.add(new TextField(IndexField.ITUNES_SUMMARY, x, Field.Store.YES)) }
      s.chapterMarks.foreach   { x => d.add(new TextField(IndexField.CHAPTER_MARKS, x, Field.Store.NO)) }
      s.contentEncoded.foreach { x => d.add(new TextField(IndexField.CONTENT_ENCODED, x, Field.Store.NO)) }
      s.transcript.foreach     { x => d.add(new TextField(IndexField.TRANSCRIPT, x, Field.Store.NO)) }
      s.websiteData.foreach    { x => d.add(new TextField(IndexField.WEBSITE_DATA, x, Field.Store.NO)) }
      d
    }
    .orNull


  def get(doc: org.apache.lucene.document.Document, fieldName: String): Option[String] = Option(doc.get(fieldName))

}
