package io.hemin.engine.util.mapper

import io.hemin.engine.model.{Episode, EpisodeItunes, IndexDoc, IndexField}
import io.hemin.engine.util.mapper.MapperErrors._
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

  def toEpisode(src: org.apache.lucene.document.Document): Try[Episode] = Option(src)
    .map { s =>
      Episode(
        id           = LuceneMapper.get(s, IndexField.Id.entryName),
        title        = LuceneMapper.get(s, IndexField.Title.entryName),
        podcastTitle = LuceneMapper.get(s, IndexField.PodcastTitle.entryName),
        link         = LuceneMapper.get(s, IndexField.Link.entryName),
        pubDate      = DateMapper.asMilliseconds(s.get(IndexField.PubDate.entryName)),
        description  = LuceneMapper.get(s, IndexField.Description.entryName),
        image        = LuceneMapper.get(s, IndexField.ItunesImage.entryName),
        itunes = EpisodeItunes(
          author   = LuceneMapper.get(s, IndexField.ItunesAuthor.entryName),
          summary  = LuceneMapper.get(s, IndexField.ItunesSummary.entryName),
          duration = LuceneMapper.get(s, IndexField.ItunesDuration.entryName),
        )
      )
    }
    .map(Success(_))
    .getOrElse(mapperFailureLuceneToEpisode(src))

  def toEpisode(src: SolrDocument): Try[Episode] = Option(src)
    .map { s =>
      Episode(
        id           = SolrMapper.firstStringMatch(s, IndexField.Id.entryName),
        title        = SolrMapper.firstStringMatch(s, IndexField.Title.entryName),
        podcastTitle = SolrMapper.firstStringMatch(s, IndexField.PodcastTitle.entryName),
        link         = SolrMapper.firstStringMatch(s, IndexField.Link.entryName),
        pubDate      = SolrMapper.firstDateMatch(s, IndexField.PubDate.entryName).flatMap(x => DateMapper.asMilliseconds(x)),
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
        image = e.image,
        itunes = EpisodeItunes(
          summary = e.itunes.summary,
        )
      )
    }

}
