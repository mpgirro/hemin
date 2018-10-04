package io.disposia.engine.newdomain

import java.time.LocalDateTime

import io.disposia.engine.domain.IndexField
import io.disposia.engine.mapper.DateMapper
import io.disposia.engine.newdomain.episode.EpisodeItunesInfo
import io.disposia.engine.newdomain.podcast.PodcastItunesInfo
import io.disposia.engine.util.mapper.reduce
import org.apache.lucene.document.{Field, StringField, TextField}
import org.apache.solr.common.SolrInputDocument


case class NewIndexDoc(
  docType: Option[String]        = None,
  id: Option[String]             = None,
  title: Option[String]          = None,
  link: Option[String]           = None,
  description: Option[String]    = None,
  pubDate: Option[LocalDateTime] = None,
  image: Option[String]          = None,
  itunesAuthor: Option[String]   = None,
  itunesSummary: Option[String]  = None,
  podcastTitle: Option[String]   = None,
  chapterMarks: Option[String]   = None,
  contentEncoded: Option[String] = None,
  transcript: Option[String]     = None,
  websiteData: Option[String]    = None
) {

  def copy(patch: NewIndexDoc): NewIndexDoc = {
    Option(patch) match {
      case None => this
      case Some(p) =>
        NewIndexDoc(
          docType        = reduce(this.docType, p.docType),
          id             = reduce(this.id, p.id),
          title          = reduce(this.title, p.title),
          link           = reduce(this.link, p.link),
          description    = reduce(this.description, p.description),
          pubDate        = reduce(this.pubDate, p.pubDate),
          image          = reduce(this.image, p.image),
          itunesAuthor   = reduce(this.itunesAuthor, p.itunesAuthor),
          itunesSummary  = reduce(this.itunesSummary, p.itunesSummary),
          podcastTitle   = reduce(this.podcastTitle, p.podcastTitle),
          chapterMarks   = reduce(this.chapterMarks, p.chapterMarks),
          contentEncoded = reduce(this.contentEncoded, p.contentEncoded),
          transcript     = reduce(this.transcript, p.transcript),
          websiteData    = reduce(this.websiteData, p.websiteData),
        )
    }
  }

  /*
  def asPodcast: NewPodcast = docType.map {
    case "podcast" =>
      NewPodcast(
        id          = this.id,
        title       = this.title,
        link        = this.link,
        description = this.description,
        pubDate     = this.pubDate,
        image       = this.image,
        itunes = PodcastItunesInfo(
          author  = this.itunesAuthor,
          summary = this.itunesSummary
        )
      )
    case other => throw new UnsupportedOperationException(s"Required docType='podcast', actual docType='$other'")
  }.orNull
  */

  /* TODO delete
  def asPodcast: NewPodcast = docType match {
    case Some(dt) => dt match {
      case "podcast" =>
        NewPodcast(
          id          = this.id,
          title       = this.title,
          link        = this.link,
          description = this.description,
          pubDate     = this.pubDate,
          image       = this.image,
          itunes      = PodcastItunesInfo(
            author  = this.itunesAuthor,
            summary = this.itunesSummary
          )
        )
      case other => throw new UnsupportedOperationException(s"Required docType='podcast', actual docType='$other'")
    }
    case None => throw new UnsupportedOperationException(s"docType is not set")
  }
  */

  /*
  def asEpisode: NewEpisode = docType.map {
    case "episode" =>
      NewEpisode(
        id          = this.id,
        title       = this.title,
        link        = this.link,
        description = this.description,
        pubDate     = this.pubDate,
        image       = this.image,
        itunes = EpisodeItunesInfo(
          author  = this.itunesAuthor,
          summary = this.itunesSummary
        )
      )
    case other => throw new UnsupportedOperationException(s"Required docType='episode', actual docType='$other'")
  }.orNull
  */

  /* TODO delete
  def asEpisode: NewEpisode = docType match {
    case Some(dt) => dt match {
      case "episode" =>
        NewEpisode(
          id          = this.id,
          title       = this.title,
          link        = this.link,
          description = this.description,
          pubDate     = this.pubDate,
          image       = this.image,
          itunes      = EpisodeItunesInfo(
            author  = this.itunesAuthor,
            summary = this.itunesSummary
          )
        )
      case other => throw new UnsupportedOperationException(s"Required docType='episode', actual docType='$other'")
    }
    case None => throw new UnsupportedOperationException(s"docType is not set")
  }
  */

  /*
  def asLucene: org.apache.lucene.document.Document = {
    val d = new org.apache.lucene.document.Document
    docType.foreach        { x => d.add(new StringField(IndexField.DOC_TYPE, x, Field.Store.YES)) }
    id.foreach             { x => d.add(new StringField(IndexField.ID, x, Field.Store.YES)) }
    title.foreach          { x => d.add(new TextField(IndexField.TITLE, x, Field.Store.YES)) }
    link.foreach           { x => d.add(new TextField(IndexField.LINK, x, Field.Store.YES)) }
    description.foreach    { x => d.add(new TextField(IndexField.DESCRIPTION, x, Field.Store.YES)) }
    podcastTitle.foreach   { x => d.add(new TextField(IndexField.PODCAST_TITLE, x, Field.Store.YES)) }
    pubDate.foreach        { x => d.add(new StringField(IndexField.PUB_DATE, DateMapper.INSTANCE.asString(x), Field.Store.YES)) }
    image.foreach          { x => d.add(new TextField(IndexField.ITUNES_IMAGE, x, Field.Store.YES)) }
    itunesAuthor.foreach   { x => d.add(new TextField(IndexField.ITUNES_AUTHOR, x, Field.Store.NO)) }
    itunesSummary.foreach  { x => d.add(new TextField(IndexField.ITUNES_SUMMARY, x, Field.Store.YES)) }
    chapterMarks.foreach   { x => d.add(new TextField(IndexField.CHAPTER_MARKS, x, Field.Store.NO)) }
    contentEncoded.foreach { x => d.add(new TextField(IndexField.CONTENT_ENCODED, x, Field.Store.NO)) }
    transcript.foreach     { x => d.add(new TextField(IndexField.TRANSCRIPT, x, Field.Store.NO)) }
    websiteData.foreach    { x => d.add(new TextField(IndexField.WEBSITE_DATA, x, Field.Store.NO)) }
    d
  }
  */

  /*
  def asSolr: SolrInputDocument = {
    val d = new SolrInputDocument
    docType.foreach        { x => d.addField(IndexField.DOC_TYPE, x) }
    id.foreach             { x => d.addField(IndexField.ID, x) }
    title.foreach          { x => d.addField(IndexField.TITLE, x) }
    link.foreach           { x => d.addField(IndexField.LINK, x) }
    description.foreach    { x => d.addField(IndexField.DESCRIPTION, x) }
    podcastTitle.foreach   { x => d.addField(IndexField.PODCAST_TITLE, x) }
    pubDate.foreach        { x => d.addField(IndexField.PUB_DATE, DateMapper.INSTANCE.asString(x)) }
    image.foreach          { x => d.addField(IndexField.ITUNES_IMAGE, x) }
    itunesAuthor.foreach   { x => d.addField(IndexField.ITUNES_AUTHOR, x) }
    itunesSummary.foreach  { x => d.addField(IndexField.ITUNES_SUMMARY, x) }
    chapterMarks.foreach   { x => d.addField(IndexField.CHAPTER_MARKS, x) }
    contentEncoded.foreach { x => d.addField(IndexField.CONTENT_ENCODED, x) }
    transcript.foreach     { x => d.addField(IndexField.TRANSCRIPT, x) }
    websiteData.foreach    { x => d.addField(IndexField.WEBSITE_DATA, x) }
    d
  }
  */

}
