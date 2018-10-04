package io.disposia.engine.util.mapper

import io.disposia.engine.domain.{IndexDoc, IndexField}
import io.disposia.engine.mapper.DateMapper
import io.disposia.engine.newdomain.NewIndexDoc
import io.disposia.engine.util.mapper.SolrMapper.dateMapper
import org.apache.lucene.document.{Field, StringField, TextField}


object NewLuceneMapper {

  def toLucene(src: IndexDoc): org.apache.lucene.document.Document =
    Option(src)
      .map { s =>
        val d = new org.apache.lucene.document.Document
        Option(s.getDocType).foreach        { x => d.add(new StringField(IndexField.DOC_TYPE, x, Field.Store.YES)) }
        Option(s.getId).foreach             { x => d.add(new StringField(IndexField.ID, x, Field.Store.YES)) }
        //Option(s.getExo).foreach            { x => d.add(new StringField(IndexField.EXO, x, Field.Store.YES)) }
        Option(s.getTitle).foreach          { x => d.add(new TextField(IndexField.TITLE, x, Field.Store.YES)) }
        Option(s.getLink).foreach           { x => d.add(new TextField(IndexField.LINK, x, Field.Store.YES)) }
        Option(s.getDescription).foreach    { x => d.add(new TextField(IndexField.DESCRIPTION, x, Field.Store.YES)) }
        Option(s.getPodcastTitle).foreach   { x => d.add(new TextField(IndexField.PODCAST_TITLE, x, Field.Store.YES)) }
        Option(s.getPubDate).foreach        { x => d.add(new StringField(IndexField.PUB_DATE, DateMapper.INSTANCE.asString(x), Field.Store.YES)) }
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

  def toLucene(src: NewIndexDoc): org.apache.lucene.document.Document =
    Option(src)
      .map { s =>
        val d = new org.apache.lucene.document.Document
        s.docType.foreach        { x => d.add(new StringField(IndexField.DOC_TYPE, x, Field.Store.YES)) }
        s.id.foreach             { x => d.add(new StringField(IndexField.ID, x, Field.Store.YES)) }
        //Option(s.getExo).foreach            { x => d.add(new StringField(IndexField.EXO, x, Field.Store.YES)) }
        s.title.foreach          { x => d.add(new TextField(IndexField.TITLE, x, Field.Store.YES)) }
        s.link.foreach           { x => d.add(new TextField(IndexField.LINK, x, Field.Store.YES)) }
        s.description.foreach    { x => d.add(new TextField(IndexField.DESCRIPTION, x, Field.Store.YES)) }
        s.podcastTitle.foreach   { x => d.add(new TextField(IndexField.PODCAST_TITLE, x, Field.Store.YES)) }
        s.pubDate.foreach        { x => d.add(new StringField(IndexField.PUB_DATE, DateMapper.INSTANCE.asString(x), Field.Store.YES)) }
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

}
