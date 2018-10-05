package io.disposia.engine.domain

import java.time.LocalDateTime

import io.disposia.engine.catalog.repository.BsonConversion
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

object Image {
  implicit val bsonWriter: BSONDocumentWriter[Image] = Macros.writer[Image]
  implicit val bsonReader: BSONDocumentReader[Image] = Macros.reader[Image]

  private implicit val bsonDateTimeWriter: BsonConversion.DateReader.type = BsonConversion.DateReader
  private implicit val bsonDateTimeReader: BsonConversion.DateWriter.type = BsonConversion.DateWriter
}

case class Image (
  id: Option[String]               = None,
  associateId: Option[String]      = None,
  data: Option[Array[Byte]]        = None,
  hash: Option[String]             = None,
  name: Option[String]             = None,
  contentType: Option[String]      = None,
  size: Option[Long]               = None,
  createdAt: Option[LocalDateTime] = None
)
