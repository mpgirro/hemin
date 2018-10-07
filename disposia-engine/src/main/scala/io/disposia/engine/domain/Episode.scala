package io.disposia.engine.domain

import java.time.LocalDateTime

import io.disposia.engine.domain.info.{EpisodeEnclosureInfo, EpisodeItunesInfo, EpisodeRegistrationInfo}
import io.disposia.engine.util.mapper.reduce

case class Episode(
  id: Option[String]                    = None,
  podcastId: Option[String]             = None,
  podcastTitle: Option[String]          = None,
  title: Option[String]                 = None,
  link: Option[String]                  = None,
  pubDate: Option[LocalDateTime]        = None,
  guid: Option[String]                  = None,
  guidIsPermalink: Option[Boolean]      = None,
  description: Option[String]           = None,
  image: Option[String]                 = None,
  contentEncoded: Option[String]        = None,
  chapters: List[Chapter]               = List(),
  itunes: EpisodeItunesInfo             = EpisodeItunesInfo(),
  enclosure: EpisodeEnclosureInfo       = EpisodeEnclosureInfo(),
  registration: EpisodeRegistrationInfo = EpisodeRegistrationInfo()
) {

  /**
    * Patches a copy of the current instance with the diff . Only non-None fields
    * of the diff overwrite the values of the current instance in the copy.
    *
    * @param diff An instance with the specific fields that get patched
    * @return The copy of this instance with the non-None fields of diff applied
    */
  def patch(diff: Episode): Episode = Option(diff) match {
    case None => this
    case Some(x) =>
      Episode(
        id              = reduce(this.id, x.id),
        podcastId       = reduce(this.podcastId, x.podcastId),
        podcastTitle    = reduce(this.podcastTitle, x.podcastTitle),
        title           = reduce(this.title, x.title),
        link            = reduce(this.link, x.link),
        pubDate         = reduce(this.pubDate, x.pubDate),
        guid            = reduce(this.guid, x.guid),
        guidIsPermalink = reduce(this.guidIsPermalink, x.guidIsPermalink),
        description     = reduce(this.description, x.description),
        image           = reduce(this.image, x.image),
        contentEncoded  = reduce(this.contentEncoded, x.contentEncoded),
        chapters        = reduce(this.chapters, x.chapters),
        itunes = EpisodeItunesInfo(
          duration    = reduce(this.itunes.duration, x.itunes.duration),
          subtitle    = reduce(this.itunes.subtitle, x.itunes.subtitle),
          author      = reduce(this.itunes.author, x.itunes.author),
          summary     = reduce(this.itunes.summary, x.itunes.summary),
          season      = reduce(this.itunes.season, x.itunes.season),
          episode     = reduce(this.itunes.episode, x.itunes.episode),
          episodeType = reduce(this.itunes.episodeType, x.itunes.episodeType),
        ),
        enclosure = EpisodeEnclosureInfo(
          url    = reduce(this.enclosure.url, x.enclosure.url),
          length = reduce(this.enclosure.length, x.enclosure.length),
          typ    = reduce(this.enclosure.typ, x.enclosure.typ),
        ),
        registration = EpisodeRegistrationInfo(
          timestamp = reduce(this.registration.timestamp, x.registration.timestamp)
        )
      )
  }

}
