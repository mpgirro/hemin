package io.disposia.engine.util.mapper

import io.disposia.engine.newdomain.episode.{EpisodeEnclosureInfo, EpisodeItunesInfo, EpisodeRegistrationInfo}
import io.disposia.engine.newdomain.{NewEpisode, NewIndexDoc}

object NewEpisodeMapper {

  def toEpisode(src: NewIndexDoc): NewEpisode =
    Option(src)
      .map{ s =>
        NewEpisode(
          id          = s.id,
          title       = s.title,
          link        = s.link,
          description = s.description,
          pubDate     = s.pubDate,
          image       = s.image,
          itunes      = EpisodeItunesInfo(
            author  = s.itunesAuthor,
            summary = s.itunesSummary
          )
        )
      }
      .orNull

  def update(current: NewEpisode, diff: NewEpisode): NewEpisode =
    (Option(current), Option(diff)) match {
      case (Some(c), None)    => c
      case (None, Some(d))    => d
      case (None, None)       => null
      case (Some(c), Some(d)) =>
        NewEpisode(
          id              = reduce(c.id, d.id),
          podcastId       = reduce(c.podcastId, d.podcastId),
          podcastTitle    = reduce(c.podcastTitle, d.podcastTitle),
          title           = reduce(c.title, d.title),
          link            = reduce(c.link, d.link),
          pubDate         = reduce(c.pubDate, d.pubDate),
          guid            = reduce(c.guid, d.guid),
          guidIsPermalink = reduce(c.guidIsPermalink, d.guidIsPermalink),
          description     = reduce(c.description, d.description),
          image           = reduce(c.image, d.image),
          contentEncoded  = reduce(c.contentEncoded, d.contentEncoded),
          chapters        = reduce(c.chapters, d.chapters),
          itunes = EpisodeItunesInfo(
            duration    = reduce(c.itunes.duration, d.itunes.duration),
            subtitle    = reduce(c.itunes.subtitle, d.itunes.subtitle),
            author      = reduce(c.itunes.author, d.itunes.author),
            summary     = reduce(c.itunes.summary, d.itunes.summary),
            season      = reduce(c.itunes.season, d.itunes.season),
            episode     = reduce(c.itunes.episode, d.itunes.episode),
            episodeType = reduce(c.itunes.episodeType, d.itunes.episodeType),
          ),
          enclosure = EpisodeEnclosureInfo(
            url    = reduce(c.enclosure.url, d.enclosure.url),
            length = reduce(c.enclosure.length, d.enclosure.length),
            typ    = reduce(c.enclosure.typ, d.enclosure.typ),
          ),
          registration = EpisodeRegistrationInfo(
            timestamp = reduce(c.registration.timestamp, d.registration.timestamp)
          )
        )
    }

}
