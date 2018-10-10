package io.hemin.engine.domain.info


case class PodcastItunesInfo(
  summary: Option[String]     = None,
  author: Option[String]      = None,
  keywords: List[String]      = Nil,
  categories: List[String]    = Nil,
  explicit: Option[Boolean]   = None,
  block: Option[Boolean]      = None,
  podcastType: Option[String] = None,
  ownerName: Option[String]   = None,
  ownerEmail: Option[String]  = None,
)
