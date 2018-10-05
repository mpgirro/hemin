package io.disposia.engine.newdomain.podcast

import java.time.LocalDateTime

import io.disposia.engine.catalog.repository.BsonConversion
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

object PodcastRegistrationInfo {
  implicit val bsonWriter: BSONDocumentWriter[PodcastRegistrationInfo] = Macros.writer[PodcastRegistrationInfo]
  implicit val bsonReader: BSONDocumentReader[PodcastRegistrationInfo] = Macros.reader[PodcastRegistrationInfo]

  private implicit val bsonDateTimeWriter: BsonConversion.DateReader.type = BsonConversion.DateReader
  private implicit val bsonDateTimeReader: BsonConversion.DateWriter.type = BsonConversion.DateWriter
}

case class PodcastRegistrationInfo(
                                     timestamp: Option[LocalDateTime] = None,
                                     complete: Option[Boolean] = None
                                   )
