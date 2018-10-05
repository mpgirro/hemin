package io.disposia.engine.newdomain.episode

import java.time.LocalDateTime

import io.disposia.engine.catalog.repository.BsonConversion
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

object EpisodeEnclosureInfo {
  implicit val bsonWriter: BSONDocumentWriter[EpisodeEnclosureInfo] = Macros.writer[EpisodeEnclosureInfo]
  implicit val bsonReader: BSONDocumentReader[EpisodeEnclosureInfo] = Macros.reader[EpisodeEnclosureInfo]

  private implicit val bsonDateTimeWriter: BsonConversion.DateReader.type = BsonConversion.DateReader
  private implicit val bsonDateTimeReader: BsonConversion.DateWriter.type = BsonConversion.DateWriter
}

case class EpisodeEnclosureInfo(
                                  url: Option[String] = None,
                                  length: Option[Long] = None,
                                  typ: Option[String] = None
                                )
