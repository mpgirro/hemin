package io.hemin.engine.model.info

import java.time.LocalDateTime

final case class PodcastMetadata(
  lastBuildDate: Option[LocalDateTime] = None,
  language: Option[String]             = None,
  generator: Option[String]            = None,
  copyright: Option[String]            = None,
  docs: Option[String]                 = None,
  managingEditor: Option[String]       = None,
)
