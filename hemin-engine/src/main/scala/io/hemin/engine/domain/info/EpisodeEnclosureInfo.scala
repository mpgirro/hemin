package io.hemin.engine.domain.info

final case class EpisodeEnclosureInfo(
  url: Option[String]  = None,
  length: Option[Long] = None,
  typ: Option[String]  = None,
)
