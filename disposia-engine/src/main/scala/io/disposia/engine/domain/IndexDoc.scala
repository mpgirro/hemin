package io.disposia.engine.domain

import java.time.LocalDateTime

import io.disposia.engine.util.mapper.reduce


case class IndexDoc(
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

  def patch(diff: IndexDoc): IndexDoc = Option(diff) match {
    case None => this
    case Some(x) =>
      IndexDoc(
        docType        = reduce(this.docType, x.docType),
        id             = reduce(this.id, x.id),
        title          = reduce(this.title, x.title),
        link           = reduce(this.link, x.link),
        description    = reduce(this.description, x.description),
        pubDate        = reduce(this.pubDate, x.pubDate),
        image          = reduce(this.image, x.image),
        itunesAuthor   = reduce(this.itunesAuthor, x.itunesAuthor),
        itunesSummary  = reduce(this.itunesSummary, x.itunesSummary),
        podcastTitle   = reduce(this.podcastTitle, x.podcastTitle),
        chapterMarks   = reduce(this.chapterMarks, x.chapterMarks),
        contentEncoded = reduce(this.contentEncoded, x.contentEncoded),
        transcript     = reduce(this.transcript, x.transcript),
        websiteData    = reduce(this.websiteData, x.websiteData),
      )
  }

}
