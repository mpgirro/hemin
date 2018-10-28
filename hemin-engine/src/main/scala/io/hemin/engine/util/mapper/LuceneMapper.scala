package io.hemin.engine.util.mapper

import io.hemin.engine.model.IndexDoc
import io.hemin.engine.util.IndexField
import org.apache.lucene.document.{Field, StringField, TextField}


object LuceneMapper {

  def toLucene(src: IndexDoc): org.apache.lucene.document.Document = Option(src)
    .map { s =>
      val d = new org.apache.lucene.document.Document
      s.docType.foreach        { x => d.add(new StringField(IndexField.DocType.entryName, x, Field.Store.YES)) }
      s.id.foreach             { x => d.add(new StringField(IndexField.Id.entryName, x, Field.Store.YES)) }
      s.title.foreach          { x => d.add(new TextField(IndexField.Title.entryName, x, Field.Store.YES)) }
      s.link.foreach           { x => d.add(new TextField(IndexField.Link.entryName, x, Field.Store.YES)) }
      s.description.foreach    { x => d.add(new TextField(IndexField.Description.entryName, x, Field.Store.YES)) }
      s.podcastTitle.foreach   { x => d.add(new TextField(IndexField.PodcastTitle.entryName, x, Field.Store.YES)) }
      s.pubDate.foreach        { x => d.add(new StringField(IndexField.PubDate.entryName, DateMapper.asString(x).get, Field.Store.YES)) }
      s.image.foreach          { x => d.add(new TextField(IndexField.ItunesImage.entryName, x, Field.Store.YES)) }
      s.itunesAuthor.foreach   { x => d.add(new TextField(IndexField.ItunesAuthor.entryName, x, Field.Store.NO)) }
      s.itunesSummary.foreach  { x => d.add(new TextField(IndexField.ItunesSummary.entryName, x, Field.Store.YES)) }
      s.chapterMarks.foreach   { x => d.add(new TextField(IndexField.ChapterMarks.entryName, x, Field.Store.NO)) }
      s.contentEncoded.foreach { x => d.add(new TextField(IndexField.ContentEncoded.entryName, x, Field.Store.NO)) }
      s.transcript.foreach     { x => d.add(new TextField(IndexField.Transcript.entryName, x, Field.Store.NO)) }
      s.websiteData.foreach    { x => d.add(new TextField(IndexField.WebsiteData.entryName, x, Field.Store.NO)) }
      d
    }
    .orNull

  def get(doc: org.apache.lucene.document.Document, fieldName: String): Option[String] = Option(doc.get(fieldName))

}
