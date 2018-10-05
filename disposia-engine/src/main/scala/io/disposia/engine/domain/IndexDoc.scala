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

  def update(patch: IndexDoc): IndexDoc = Option(patch) match {
    case None => this
    case Some(p) =>
      IndexDoc(
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
