package io.disposia.engine.newdomain

import java.time.LocalDateTime

import io.disposia.engine.catalog.repository.BsonConversion
import io.disposia.engine.newdomain.podcast._
import io.disposia.engine.util.mapper.reduce
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

object NewPodcast {
  implicit val bsonWriter: BSONDocumentWriter[NewPodcast] = Macros.writer[NewPodcast]
  implicit val bsonReader: BSONDocumentReader[NewPodcast] = Macros.reader[NewPodcast]

  private implicit val bsonDateTimeWriter: BsonConversion.DateReader.type = BsonConversion.DateReader
  private implicit val bsonDateTimeReader: BsonConversion.DateWriter.type = BsonConversion.DateWriter
}

case class NewPodcast(
  id: Option[String]                    = None,
  title: Option[String]                 = None,
  link: Option[String]                  = None,
  description: Option[String]           = None,
  pubDate: Option[LocalDateTime]        = None,
  image: Option[String]                 = None,
  meta: PodcastMetadata                 = PodcastMetadata(),
  registration: PodcastRegistrationInfo = PodcastRegistrationInfo(),
  itunes: PodcastItunesInfo             = PodcastItunesInfo(),
  feedpress: PodcastFeedpressInfo       = PodcastFeedpressInfo(),
  fyyd: PodcastFyydInfo                 = PodcastFyydInfo()
) {

  def update(patch: NewPodcast): NewPodcast = {
    Option(patch) match {
      case None => this
      case Some(p) =>
        NewPodcast(
          id              = reduce(this.id, p.id),
          title           = reduce(this.title, p.title),
          link            = reduce(this.link, p.link),
          description     = reduce(this.description, p.description),
          pubDate         = reduce(this.pubDate, p.pubDate),
          image           = reduce(this.image, p.image),
          meta = PodcastMetadata(
            lastBuildDate  = reduce(this.meta.lastBuildDate, p.meta.lastBuildDate),
            language       = reduce(this.meta.language, p.meta.language),
            generator      = reduce(this.meta.generator, p.meta.generator),
            copyright      = reduce(this.meta.copyright, p.meta.copyright),
            docs           = reduce(this.meta.docs, p.meta.docs),
            managingEditor = reduce(this.meta.managingEditor, p.meta.managingEditor),
          ),
          registration = PodcastRegistrationInfo(
            timestamp = reduce(this.registration.timestamp, p.registration.timestamp),
            complete  = reduce(this.registration.complete, p.registration.complete),
          ),
          itunes = PodcastItunesInfo(
            summary     = reduce(this.itunes.summary, p.itunes.summary),
            author      = reduce(this.itunes.author, p.itunes.author),
            keywords    = reduce(this.itunes.keywords, p.itunes.keywords),
            categories  = reduce(this.itunes.categories, p.itunes.categories),
            explicit    = reduce(this.itunes.explicit, p.itunes.explicit),
            block       = reduce(this.itunes.block, p.itunes.block),
            podcastType = reduce(this.itunes.podcastType, p.itunes.podcastType),
            ownerName   = reduce(this.itunes.ownerName, p.itunes.ownerName),
            ownerEmail  = reduce(this.itunes.ownerEmail, p.itunes.ownerEmail),
          ),
          feedpress = PodcastFeedpressInfo(
            locale = reduce(this.feedpress.locale, p.feedpress.locale),
          ),
          fyyd = PodcastFyydInfo(
            verify = reduce(this.fyyd.verify, p.fyyd.verify),
          )
        )
    }
  }

}
