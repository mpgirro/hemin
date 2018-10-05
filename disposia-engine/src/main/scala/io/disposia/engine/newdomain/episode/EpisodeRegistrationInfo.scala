package io.disposia.engine.newdomain.episode

import java.time.LocalDateTime

import io.disposia.engine.catalog.repository.BsonConversion
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

object EpisodeRegistrationInfo {
  implicit val bsonWriter: BSONDocumentWriter[EpisodeRegistrationInfo] = Macros.writer[EpisodeRegistrationInfo]
  implicit val bsonReader: BSONDocumentReader[EpisodeRegistrationInfo] = Macros.reader[EpisodeRegistrationInfo]

  private implicit val bsonDateTimeWriter: BsonConversion.DateReader.type = BsonConversion.DateReader
  private implicit val bsonDateTimeReader: BsonConversion.DateWriter.type = BsonConversion.DateWriter
}

case class EpisodeRegistrationInfo(timestamp: Option[LocalDateTime] = None)
