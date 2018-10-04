package io.disposia.engine.newdomain

import java.time.LocalDateTime

import io.disposia.engine.newdomain.episode.{EpisodeEnclosureInfo, EpisodeItunesInfo, EpisodeRegistrationInfo}

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
)
