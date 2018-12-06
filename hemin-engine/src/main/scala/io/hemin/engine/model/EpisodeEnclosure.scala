package io.hemin.engine.model

/**
  * @author max
  */
final case class EpisodeEnclosure(
  url: Option[String]  = None,
  length: Option[Long] = None,
  typ: Option[String]  = None,
)
