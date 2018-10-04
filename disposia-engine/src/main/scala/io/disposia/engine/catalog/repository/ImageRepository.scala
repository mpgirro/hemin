package io.disposia.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.disposia.engine.catalog.repository.BsonConversion._
import io.disposia.engine.newdomain.Image
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import scala.concurrent.{ExecutionContext, Future}


class ImageRepository (db: DefaultDB, ec: ExecutionContext)
  extends MongoRepository[Image] {

  override protected[this] def log: Logger = Logger(getClass)

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] implicit def bsonWriter: BSONDocumentWriter[Image] = BsonConversion.bsonImageWriter

  override protected[this] implicit def bsonReader: BSONDocumentReader[Image] = BsonConversion.bsonImageReader

  override protected[this] def collection: BSONCollection = db.apply("images")

  override def save(image: Image): Future[Image] = {
    val query = BSONDocument("id" -> image.id)
    collection
      .update(query, image, upsert = true)
      .flatMap { _ => findOne(image.id)
        .map {
          case Some(i) => i
          case None    => throw new RuntimeException("Saving Image to database was unsuccessful : " + image)
        }
      }
  }

  override def findOne(id: String): Future[Option[Image]] = {
    log.debug("Request to get Image (ID) : {}", id)
    val query = toDocument(Map("id" -> toBsonS(id)))
    findOne(query)
  }

  def findOneByAssociate(id: String): Future[Option[Image]] = {
    log.debug("Request to get Image (ID) : {}", id)
    val query = toDocument(Map("associateId" -> toBsonS(id)))
    findOne(query)
  }

}
