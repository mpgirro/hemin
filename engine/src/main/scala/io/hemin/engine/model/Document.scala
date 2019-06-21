package io.hemin.engine.model

final case class Document(
  documentType: Option[String]   = None,
  id: Option[String]             = None,
  title: Option[String]          = None,
  link: Option[String]           = None,
  description: Option[String]    = None,
  pubDate: Option[Long]          = None,
  image: Option[String]          = None,
  itunesAuthor: Option[String]   = None,
  itunesSummary: Option[String]  = None,
  podcastTitle: Option[String]   = None,
  chapterMarks: Option[String]   = None,
  contentEncoded: Option[String] = None,
  transcript: Option[String]     = None,
  websiteData: Option[String]    = None,
) extends Patchable[Document] {

  override def patchLeft(diff: Document): Document = Option(diff) match {
    case None       => this
    case Some(that) => Document(
      documentType   = reduceLeft(this.documentType, that.documentType),
      id             = reduceLeft(this.id, that.id),
      title          = reduceLeft(this.title, that.title),
      link           = reduceLeft(this.link, that.link),
      description    = reduceLeft(this.description, that.description),
      pubDate        = reduceLeft(this.pubDate, that.pubDate),
      image          = reduceLeft(this.image, that.image),
      itunesAuthor   = reduceLeft(this.itunesAuthor, that.itunesAuthor),
      itunesSummary  = reduceLeft(this.itunesSummary, that.itunesSummary),
      podcastTitle   = reduceLeft(this.podcastTitle, that.podcastTitle),
      chapterMarks   = reduceLeft(this.chapterMarks, that.chapterMarks),
      contentEncoded = reduceLeft(this.contentEncoded, that.contentEncoded),
      transcript     = reduceLeft(this.transcript, that.transcript),
      websiteData    = reduceLeft(this.websiteData, that.websiteData),
    )
  }

  override def patchRight(diff: Document): Document = Option(diff) match {
    case None       => this
    case Some(that) => Document(
      documentType   = reduceRight(this.documentType, that.documentType),
      id             = reduceRight(this.id, that.id),
      title          = reduceRight(this.title, that.title),
      link           = reduceRight(this.link, that.link),
      description    = reduceRight(this.description, that.description),
      pubDate        = reduceRight(this.pubDate, that.pubDate),
      image          = reduceRight(this.image, that.image),
      itunesAuthor   = reduceRight(this.itunesAuthor, that.itunesAuthor),
      itunesSummary  = reduceRight(this.itunesSummary, that.itunesSummary),
      podcastTitle   = reduceRight(this.podcastTitle, that.podcastTitle),
      chapterMarks   = reduceRight(this.chapterMarks, that.chapterMarks),
      contentEncoded = reduceRight(this.contentEncoded, that.contentEncoded),
      transcript     = reduceRight(this.transcript, that.transcript),
      websiteData    = reduceRight(this.websiteData, that.websiteData),
    )
  }
}
