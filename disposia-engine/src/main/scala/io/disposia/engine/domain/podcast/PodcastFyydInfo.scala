package io.disposia.engine.domain.podcast

import io.disposia.engine.catalog.repository.BsonConversion
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

object PodcastFyydInfo {
  implicit val bsonWriter: BSONDocumentWriter[PodcastFyydInfo] = Macros.writer[PodcastFyydInfo]
  implicit val bsonReader: BSONDocumentReader[PodcastFyydInfo] = Macros.reader[PodcastFyydInfo]

  private implicit val bsonDateTimeWriter: BsonConversion.DateReader.type = BsonConversion.DateReader
  private implicit val bsonDateTimeReader: BsonConversion.DateWriter.type = BsonConversion.DateWriter
}

case class PodcastFyydInfo(
                           verify: Option[String] = None
                           )
