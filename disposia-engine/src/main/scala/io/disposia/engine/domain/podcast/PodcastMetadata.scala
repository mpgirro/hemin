package io.disposia.engine.domain.podcast

import java.time.LocalDateTime

import io.disposia.engine.catalog.repository.BsonConversion
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

object PodcastMetadata {
  implicit val bsonWriter: BSONDocumentWriter[PodcastMetadata] = Macros.writer[PodcastMetadata]
  implicit val bsonReader: BSONDocumentReader[PodcastMetadata] = Macros.reader[PodcastMetadata]

  private implicit val bsonDateTimeWriter: BsonConversion.DateReader.type = BsonConversion.DateReader
  private implicit val bsonDateTimeReader: BsonConversion.DateWriter.type = BsonConversion.DateWriter
}

case class PodcastMetadata (
                             lastBuildDate: Option[LocalDateTime] = None,
                             language: Option[String] = None,
                             generator: Option[String] = None,
                             copyright: Option[String] = None,
                             docs: Option[String] = None,
                             managingEditor: Option[String] = None
                           )
