package io.disposia.engine.newdomain.podcast

/**
  * @author max
  */
case class PodcastItunesInfo (
                               summary: Option[String] = None,
                               author: Option[String] = None,
                               keywords: Option[String] = None,
                               categories: Option[Set[String]] = None,
                               explicit: Option[String] = None,
                               block: Option[String] = None,
                               podcastType: Option[String] = None,
                               ownerName: Option[String] = None,
                               ownerEmail: Option[String] = None
                             )
