package io.disposia.engine.domain.episode

case class EpisodeEnclosureInfo(
  url: Option[String]  = None,
  length: Option[Long] = None,
  typ: Option[String]  = None
)
