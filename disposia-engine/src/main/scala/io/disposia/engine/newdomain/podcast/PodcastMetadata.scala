package io.disposia.engine.newdomain.podcast

/**
  * @author max
  */
case class PodcastMetadata (
                             lastBuildDate: Option[String] = None,
                             language: Option[String] = None,
                             generator: Option[String] = None,
                             copyright: Option[String] = None,
                             docs: Option[String] = None,
                             managingEditor: Option[String] = None
                           )
