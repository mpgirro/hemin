package hemin.engine.util.mapper

import hemin.engine.model.{Episode, EpisodeItunes, IndexDoc, IndexField}
import hemin.engine.util.mapper.MapperErrors._
import org.apache.solr.common.SolrDocument

import scala.util.{Success, Try}

object EpisodeMapper {

  def toEpisode(src: IndexDoc): Try[Episode] = Option(src)
    .map{ s =>
      Episode(
        id          = s.id,
        title       = s.title,
        link        = s.link,
        description = s.description,
        pubDate     = s.pubDate,
        image       = s.image,
        itunes = EpisodeItunes(
          author  = s.itunesAuthor,
          summary = s.itunesSummary,
          //duration = s.itunesDuration,
        )
      )
    }
    .map(Success(_))
    .getOrElse(mapperFailureIndexToEpisode(src))

  def toEpisode(src: SolrDocument): Try[Episode] = Option(src)
    .map { s =>
      Episode(
        id           = SolrMapper.firstStringMatch(s, IndexField.Id.entryName),
        title        = SolrMapper.firstStringMatch(s, IndexField.Title.entryName),
        podcastTitle = SolrMapper.firstStringMatch(s, IndexField.PodcastTitle.entryName),
        link         = SolrMapper.firstStringMatch(s, IndexField.Link.entryName),
        pubDate      = SolrMapper.firstLongMatch(s, IndexField.PubDate.entryName),
        description  = SolrMapper.firstStringMatch(s, IndexField.Description.entryName),
        image        = SolrMapper.firstStringMatch(s, IndexField.ItunesImage.entryName),
        itunes = EpisodeItunes(
          author   = SolrMapper.firstStringMatch(s, IndexField.ItunesAuthor.entryName),
          summary  = SolrMapper.firstStringMatch(s, IndexField.ItunesSummary.entryName),
          duration = SolrMapper.firstStringMatch(s, IndexField.ItunesDuration.entryName),
        )
      )
    }
    .map(Success(_))
    .getOrElse(mapperFailureSolrToEpisode(src))

  def toTeaser(episode: Episode): Option[Episode] = Option(episode)
    .map{ e =>
      Episode(
        id = e.id,
        podcastId = e.podcastId,
        podcastTitle = e.podcastTitle,
        title = e.title,
        link = e.link,
        pubDate = e.pubDate,
        description = (e.description, e.itunes.summary, e.contentEncoded) match {
          case (Some(d), _, _)       => Some(d)
          case (None, Some(s), _)    => Some(s)
          case (None, None, Some(c)) => Some(c)
          case (_, _, _)             => None
        },
        image = e.image,
      )
    }

}
