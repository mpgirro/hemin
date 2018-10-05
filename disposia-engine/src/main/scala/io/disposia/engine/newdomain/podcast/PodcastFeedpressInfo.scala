package io.disposia.engine.newdomain.podcast

import io.disposia.engine.catalog.repository.BsonConversion
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

object PodcastFeedpressInfo {
  implicit val bsonWriter: BSONDocumentWriter[PodcastFeedpressInfo] = Macros.writer[PodcastFeedpressInfo]
  implicit val bsonReader: BSONDocumentReader[PodcastFeedpressInfo] = Macros.reader[PodcastFeedpressInfo]

  private implicit val bsonDateTimeWriter: BsonConversion.DateReader.type = BsonConversion.DateReader
  private implicit val bsonDateTimeReader: BsonConversion.DateWriter.type = BsonConversion.DateWriter
}

case class PodcastFeedpressInfo(
                                 locale: Option[String] = None
                               )
