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

  def update(orig: NewEpisode, diff: NewEpisode): NewEpisode =
    (Option(orig), Option(diff)) match {
      case (Some(o), None)    => o
      case (None, Some(d))    => d
      case (None, None)       => null
      case (Some(o), Some(d)) =>
        NewEpisode(
          id              = reduce(o.id, d.id),
          podcastId       = reduce(o.podcastId, d.podcastId),
          podcastTitle    = reduce(o.podcastTitle, d.podcastTitle),
          title           = reduce(o.title, d.title),
          link            = reduce(o.link, d.link),
          pubDate         = reduce(o.pubDate, d.pubDate),
          guid            = reduce(o.guid, d.guid),
          guidIsPermalink = reduce(o.guidIsPermalink, d.guidIsPermalink),
          description     = reduce(o.description, d.description),
          image           = reduce(o.image, d.image),
          contentEncoded  = reduce(o.contentEncoded, d.contentEncoded),
          chapters        = reduce(o.chapters, d.chapters),
          itunes = EpisodeItunesInfo(
            duration    = reduce(o.itunes.duration, d.itunes.duration),
            subtitle    = reduce(o.itunes.subtitle, d.itunes.subtitle),
            author      = reduce(o.itunes.author, d.itunes.author),
            summary     = reduce(o.itunes.summary, d.itunes.summary),
            season      = reduce(o.itunes.season, d.itunes.season),
            episode     = reduce(o.itunes.episode, d.itunes.episode),
            episodeType = reduce(o.itunes.episodeType, d.itunes.episodeType),
          ),
          enclosure = EpisodeEnclosureInfo(
            url    = reduce(o.enclosure.url, d.enclosure.url),
            length = reduce(o.enclosure.length, d.enclosure.length),
            typ    = reduce(o.enclosure.typ, d.enclosure.typ),
          ),
          registration = EpisodeRegistrationInfo(
            timestamp = reduce(o.registration.timestamp, d.registration.timestamp)
          )
        )
    }

}
