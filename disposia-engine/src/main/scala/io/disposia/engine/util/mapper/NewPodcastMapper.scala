package io.disposia.engine.util.mapper

import io.disposia.engine.newdomain.podcast.PodcastItunesInfo
import io.disposia.engine.newdomain.{NewIndexDoc, NewPodcast}


object NewPodcastMapper {

  def toPodcast(src: NewIndexDoc): NewPodcast =
    Option(src)
      .map{ s =>
        NewPodcast(
          id          = s.id,
          title       = s.title,
          link        = s.link,
          description = s.description,
          pubDate     = s.pubDate,
          image       = s.image,
          itunes      = PodcastItunesInfo(
            author  = s.itunesAuthor,
            summary = s.itunesSummary
          )
        )
      }
      .orNull

}
