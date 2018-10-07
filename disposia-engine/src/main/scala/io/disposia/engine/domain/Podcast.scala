package io.disposia.engine.domain

import java.time.LocalDateTime

import io.disposia.engine.domain.info._
import io.disposia.engine.util.mapper.reduce


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
  fyyd: PodcastFyydInfo                 = PodcastFyydInfo()
) extends Patchable[Podcast] {

  def patch(diff: Podcast): Podcast = Option(diff) match {
    case None => this
    case Some(x) =>
      Podcast(
        id              = reduce(this.id, x.id),
        title           = reduce(this.title, x.title),
        link            = reduce(this.link, x.link),
        description     = reduce(this.description, x.description),
        pubDate         = reduce(this.pubDate, x.pubDate),
        image           = reduce(this.image, x.image),
        meta = PodcastMetadata(
          lastBuildDate  = reduce(this.meta.lastBuildDate, x.meta.lastBuildDate),
          language       = reduce(this.meta.language, x.meta.language),
          generator      = reduce(this.meta.generator, x.meta.generator),
          copyright      = reduce(this.meta.copyright, x.meta.copyright),
          docs           = reduce(this.meta.docs, x.meta.docs),
          managingEditor = reduce(this.meta.managingEditor, x.meta.managingEditor),
        ),
        registration = PodcastRegistrationInfo(
          timestamp = reduce(this.registration.timestamp, x.registration.timestamp),
          complete  = reduce(this.registration.complete, x.registration.complete),
        ),
        itunes = PodcastItunesInfo(
          summary     = reduce(this.itunes.summary, x.itunes.summary),
          author      = reduce(this.itunes.author, x.itunes.author),
          keywords    = reduce(this.itunes.keywords, x.itunes.keywords),
          categories  = reduce(this.itunes.categories, x.itunes.categories),
          explicit    = reduce(this.itunes.explicit, x.itunes.explicit),
          block       = reduce(this.itunes.block, x.itunes.block),
          podcastType = reduce(this.itunes.podcastType, x.itunes.podcastType),
          ownerName   = reduce(this.itunes.ownerName, x.itunes.ownerName),
          ownerEmail  = reduce(this.itunes.ownerEmail, x.itunes.ownerEmail),
        ),
        feedpress = PodcastFeedpressInfo(
          locale = reduce(this.feedpress.locale, x.feedpress.locale),
        ),
        fyyd = PodcastFyydInfo(
          verify = reduce(this.fyyd.verify, x.fyyd.verify),
        )
      )
  }

}
