package io.disposia.engine.experimental

import java.time.LocalDateTime


case class ExperimentalPodcast (
                               id: Option[String] = None,
                               title: Option[String] = None,
                               link: Option[String] = None,
                               description: Option[String] = None,
                               pubDate: Option[LocalDateTime] = None,
                               image: Option[String] = None,
                               meta: PodcastMetadata = PodcastMetadata(),
                               registration: PodcastRegistrationInfo = PodcastRegistrationInfo(),
                               itunes: PodcastItunesInfo = PodcastItunesInfo(),
                               feedpress: PodcastFeedpressInfo = PodcastFeedpressInfo(),
                               fyyd: PodcastFyydInfo = PodcastFyydInfo()
                               )
