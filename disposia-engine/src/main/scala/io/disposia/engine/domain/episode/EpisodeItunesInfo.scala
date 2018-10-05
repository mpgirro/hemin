package io.disposia.engine.domain.episode

import io.disposia.engine.catalog.repository.BsonConversion
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

object EpisodeItunesInfo {
  implicit val bsonWriter: BSONDocumentWriter[EpisodeItunesInfo] = Macros.writer[EpisodeItunesInfo]
  implicit val bsonReader: BSONDocumentReader[EpisodeItunesInfo] = Macros.reader[EpisodeItunesInfo]

  private implicit val bsonDateTimeWriter: BsonConversion.DateReader.type = BsonConversion.DateReader
  private implicit val bsonDateTimeReader: BsonConversion.DateWriter.type = BsonConversion.DateWriter
}

case class EpisodeItunesInfo(
                               duration: Option[String] = None,
                               subtitle: Option[String] = None,
                               author: Option[String] = None,
                               summary: Option[String] = None,
                               season: Option[Int] = None,
                               episode: Option[Int] = None,
                               episodeType: Option[String] = None
                             )
