package io.disposia.engine.domain

import java.time.LocalDateTime

import io.disposia.engine.domain.info._


case class Podcast(
  id: Option[String]                    = None,
  title: Option[String]                 = None,
  link: Option[String]                  = None,
  description: Option[String]           = None,
  pubDate: Option[LocalDateTime]        = None,
  image: Option[String]                 = None,
  meta: PodcastMetadata                 = PodcastMetadata(),
  registration: PodcastRegistrationInfo = PodcastRegistrationInfo(),
  itunes: PodcastItunesInfo             = PodcastItunesInfo(),
  feedpress: PodcastFeedpressInfo       = PodcastFeedpressInfo(),
  fyyd: PodcastFyydInfo                 = PodcastFyydInfo(),
) extends Patchable[Podcast] {

  override def patchLeft(diff: Podcast): Podcast = Option(diff) match {
    case None => this
    case Some(that) => Podcast(
      id              = reduceLeft(this.id, that.id),
      title           = reduceLeft(this.title, that.title),
      link            = reduceLeft(this.link, that.link),
      description     = reduceLeft(this.description, that.description),
      pubDate         = reduceLeft(this.pubDate, that.pubDate),
      image           = reduceLeft(this.image, that.image),
      meta = PodcastMetadata(
        lastBuildDate  = reduceLeft(this.meta.lastBuildDate, that.meta.lastBuildDate),
        language       = reduceLeft(this.meta.language, that.meta.language),
        generator      = reduceLeft(this.meta.generator, that.meta.generator),
        copyright      = reduceLeft(this.meta.copyright, that.meta.copyright),
        docs           = reduceLeft(this.meta.docs, that.meta.docs),
        managingEditor = reduceLeft(this.meta.managingEditor, that.meta.managingEditor),
      ),
      registration = PodcastRegistrationInfo(
        timestamp = reduceLeft(this.registration.timestamp, that.registration.timestamp),
        complete  = reduceLeft(this.registration.complete, that.registration.complete),
      ),
      itunes = PodcastItunesInfo(
        summary     = reduceLeft(this.itunes.summary, that.itunes.summary),
        author      = reduceLeft(this.itunes.author, that.itunes.author),
        keywords    = reduceLeft(this.itunes.keywords, that.itunes.keywords),
        categories  = reduceLeft(this.itunes.categories, that.itunes.categories),
        explicit    = reduceLeft(this.itunes.explicit, that.itunes.explicit),
        block       = reduceLeft(this.itunes.block, that.itunes.block),
        podcastType = reduceLeft(this.itunes.podcastType, that.itunes.podcastType),
        ownerName   = reduceLeft(this.itunes.ownerName, that.itunes.ownerName),
        ownerEmail  = reduceLeft(this.itunes.ownerEmail, that.itunes.ownerEmail),
      ),
      feedpress = PodcastFeedpressInfo(
        locale = reduceLeft(this.feedpress.locale, that.feedpress.locale),
      ),
      fyyd = PodcastFyydInfo(
        verify = reduceLeft(this.fyyd.verify, that.fyyd.verify),
      )
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
      image           = reduceRight(this.image, that.image),
      meta = PodcastMetadata(
        lastBuildDate  = reduceRight(this.meta.lastBuildDate, that.meta.lastBuildDate),
        language       = reduceRight(this.meta.language, that.meta.language),
        generator      = reduceRight(this.meta.generator, that.meta.generator),
        copyright      = reduceRight(this.meta.copyright, that.meta.copyright),
        docs           = reduceRight(this.meta.docs, that.meta.docs),
        managingEditor = reduceRight(this.meta.managingEditor, that.meta.managingEditor),
      ),
      registration = PodcastRegistrationInfo(
        timestamp = reduceRight(this.registration.timestamp, that.registration.timestamp),
        complete  = reduceRight(this.registration.complete, that.registration.complete),
      ),
      itunes = PodcastItunesInfo(
        summary     = reduceRight(this.itunes.summary, that.itunes.summary),
        author      = reduceRight(this.itunes.author, that.itunes.author),
        keywords    = reduceRight(this.itunes.keywords, that.itunes.keywords),
        categories  = reduceRight(this.itunes.categories, that.itunes.categories),
        explicit    = reduceRight(this.itunes.explicit, that.itunes.explicit),
        block       = reduceRight(this.itunes.block, that.itunes.block),
        podcastType = reduceRight(this.itunes.podcastType, that.itunes.podcastType),
        ownerName   = reduceRight(this.itunes.ownerName, that.itunes.ownerName),
        ownerEmail  = reduceRight(this.itunes.ownerEmail, that.itunes.ownerEmail),
      ),
      feedpress = PodcastFeedpressInfo(
        locale = reduceRight(this.feedpress.locale, that.feedpress.locale),
      ),
      fyyd = PodcastFyydInfo(
        verify = reduceRight(this.fyyd.verify, that.fyyd.verify),
      )
    )
  }
}
