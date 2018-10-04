package io.disposia.engine.newdomain

import java.time.LocalDateTime

import io.disposia.engine.newdomain.episode.{EpisodeEnclosureInfo, EpisodeItunesInfo, EpisodeRegistrationInfo}
import io.disposia.engine.util.mapper.reduce

case class NewEpisode(
  id: Option[String]                    = None,
  podcastId: Option[String]             = None,
  podcastTitle: Option[String]          = None,
  title: Option[String]                 = None,
  link: Option[String]                  = None,
  pubDate: Option[LocalDateTime]        = None,
  guid: Option[String]                  = None,
  guidIsPermalink: Option[String]       = None,
  description: Option[String]           = None,
  image: Option[String]                 = None,
  contentEncoded: Option[String]        = None,
  chapters: List[NewChapter]            = List(),
  itunes: EpisodeItunesInfo             = EpisodeItunesInfo(),
  enclosure: EpisodeEnclosureInfo       = EpisodeEnclosureInfo(),
  registration: EpisodeRegistrationInfo = EpisodeRegistrationInfo()
) {

  def update(patch: NewEpisode): NewEpisode = {
    Option(patch) match {
      case None => this
      case Some(p) =>
        NewEpisode(
          id              = reduce(this.id, p.id),
          podcastId       = reduce(this.podcastId, p.podcastId),
          podcastTitle    = reduce(this.podcastTitle, p.podcastTitle),
          title           = reduce(this.title, p.title),
          link            = reduce(this.link, p.link),
          pubDate         = reduce(this.pubDate, p.pubDate),
          guid            = reduce(this.guid, p.guid),
          guidIsPermalink = reduce(this.guidIsPermalink, p.guidIsPermalink),
          description     = reduce(this.description, p.description),
          image           = reduce(this.image, p.image),
          contentEncoded  = reduce(this.contentEncoded, p.contentEncoded),
          chapters        = reduce(this.chapters, p.chapters),
          itunes = EpisodeItunesInfo(
            duration    = reduce(this.itunes.duration, p.itunes.duration),
            subtitle    = reduce(this.itunes.subtitle, p.itunes.subtitle),
            author      = reduce(this.itunes.author, p.itunes.author),
            summary     = reduce(this.itunes.summary, p.itunes.summary),
            season      = reduce(this.itunes.season, p.itunes.season),
            episode     = reduce(this.itunes.episode, p.itunes.episode),
            episodeType = reduce(this.itunes.episodeType, p.itunes.episodeType),
          ),
          enclosure = EpisodeEnclosureInfo(
            url    = reduce(this.enclosure.url, p.enclosure.url),
            length = reduce(this.enclosure.length, p.enclosure.length),
            typ    = reduce(this.enclosure.typ, p.enclosure.typ),
          ),
          registration = EpisodeRegistrationInfo(
            timestamp = reduce(this.registration.timestamp, p.registration.timestamp)
          )
        )
    }
  }

}
