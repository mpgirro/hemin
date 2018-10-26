package io.hemin.engine.model.info

final case class EpisodeItunesInfo(
  duration: Option[String]    = None,
  subtitle: Option[String]    = None,
  author: Option[String]      = None,
  summary: Option[String]     = None,
  season: Option[Int]         = None,
  episode: Option[Int]        = None,
  episodeType: Option[String] = None,
)