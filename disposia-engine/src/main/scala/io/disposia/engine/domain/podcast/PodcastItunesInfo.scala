package io.disposia.engine.domain.podcast

import io.disposia.engine.catalog.repository.BsonConversion
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

object PodcastItunesInfo {
  implicit val bsonWriter: BSONDocumentWriter[PodcastItunesInfo] = Macros.writer[PodcastItunesInfo]
  implicit val bsonReader: BSONDocumentReader[PodcastItunesInfo] = Macros.reader[PodcastItunesInfo]

  private implicit val bsonDateTimeWriter: BsonConversion.DateReader.type = BsonConversion.DateReader
  private implicit val bsonDateTimeReader: BsonConversion.DateWriter.type = BsonConversion.DateWriter
}

case class PodcastItunesInfo (
                               summary: Option[String] = None,
                               author: Option[String] = None,
                               keywords: Option[Array[String]] = None,
                               categories: Option[Set[String]] = None,
                               explicit: Option[Boolean] = None,
                               block: Option[Boolean] = None,
                               podcastType: Option[String] = None,
                               ownerName: Option[String] = None,
                               ownerEmail: Option[String] = None
                             )
