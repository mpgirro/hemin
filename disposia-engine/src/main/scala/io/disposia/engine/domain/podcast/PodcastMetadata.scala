package io.disposia.engine.domain.podcast

import java.time.LocalDateTime

case class PodcastMetadata (
                             lastBuildDate: Option[LocalDateTime] = None,
                             language: Option[String] = None,
                             generator: Option[String] = None,
                             copyright: Option[String] = None,
                             docs: Option[String] = None,
                             managingEditor: Option[String] = None
                           )
