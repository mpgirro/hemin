package io.hemin.engine.model

import java.time.LocalDateTime

final case class Podcast(
  id: Option[String]                = None,
  title: Option[String]             = None,
  link: Option[String]              = None,
  description: Option[String]       = None,
  pubDate: Option[Long]             = None,
  lastBuildDate: Option[Long]       = None,
  language: Option[String]          = None,
  generator: Option[String]         = None,
  copyright: Option[String]         = None,
  docs: Option[String]              = None,
  managingEditor: Option[String]    = None,
  image: Option[String]             = None,
  atomLinks: List[AtomLink]         = Nil,
  registration: PodcastRegistration = PodcastRegistration(),
  itunes: PodcastItunes             = PodcastItunes(),
  feedpress: PodcastFeedpress       = PodcastFeedpress(),
  fyyd: PodcastFyyd                 = PodcastFyyd(),
) extends Patchable[Podcast] {

  override def patchLeft(diff: Podcast): Podcast = Option(diff) match {
    case None       => this
    case Some(that) => Podcast(
      id              = reduceLeft(this.id, that.id),
      title           = reduceLeft(this.title, that.title),
      link            = reduceLeft(this.link, that.link),
      description     = reduceLeft(this.description, that.description),
      pubDate         = reduceLeft(this.pubDate, that.pubDate),
      lastBuildDate   = reduceLeft(this.lastBuildDate, that.lastBuildDate),
      language        = reduceLeft(this.language, that.language),
      generator       = reduceLeft(this.generator, that.generator),
      copyright       = reduceLeft(this.copyright, that.copyright),
      docs            = reduceLeft(this.docs, that.docs),
      managingEditor  = reduceLeft(this.managingEditor, that.managingEditor),
      image           = reduceLeft(this.image, that.image),
      atomLinks       = reduceLeft(this.atomLinks, that.atomLinks),
      registration    = this.registration.patchLeft(that.registration),
      itunes          = this.itunes.patchLeft(that.itunes),
      feedpress       = this.feedpress.patchLeft(that.feedpress),
      fyyd            = this.fyyd.patchLeft(that.fyyd),
    )
  }

  override def patchRight(diff: Podcast): Podcast = Option(diff) match {
    case None => this
    case Some(that) => Podcast(
      id              = reduceRight(this.id, that.id),
      title           = reduceRight(this.title, that.title),
      link            = reduceRight(this.link, that.link),
      description     = reduceRight(this.description, that.description),
      pubDate         = reduceRight(this.pubDate, that.pubDate),
      lastBuildDate   = reduceRight(this.lastBuildDate, that.lastBuildDate),
      language        = reduceRight(this.language, that.language),
      generator       = reduceRight(this.generator, that.generator),
      copyright       = reduceRight(this.copyright, that.copyright),
      docs            = reduceRight(this.docs, that.docs),
      managingEditor  = reduceRight(this.managingEditor, that.managingEditor),
      image           = reduceRight(this.image, that.image),
      atomLinks       = reduceRight(this.atomLinks, that.atomLinks),
      registration    = this.registration.patchRight(that.registration),
      itunes          = this.itunes.patchRight(that.itunes),
      feedpress       = this.feedpress.patchRight(that.feedpress),
      fyyd            = this.fyyd.patchRight(that.fyyd),
    )
  }
}
