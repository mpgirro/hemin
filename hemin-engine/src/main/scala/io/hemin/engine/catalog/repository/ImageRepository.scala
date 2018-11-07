package io.hemin.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.hemin.engine.EngineException
import io.hemin.engine.catalog.repository.BsonConversion._
import io.hemin.engine.model.Image
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import scala.concurrent.{ExecutionContext, Future}


class ImageRepository (db: Future[DefaultDB], ec: ExecutionContext)
  extends MongoRepository[Image] {

  override protected[this] val log: Logger = Logger(getClass)

  override protected[this] implicit val executionContext: ExecutionContext = ec

  override protected[this] implicit val bsonWriter: BSONDocumentWriter[Image] = BsonConversion.imageWriter

  override protected[this] implicit val bsonReader: BSONDocumentReader[Image] = BsonConversion.imageReader

  override protected[this] val sort: BSONDocument = BSONDocument("createdAt" -> 1) // sort ascending by title

  override protected[this] def collection: Future[BSONCollection] = db.map(_.collection("images"))

  override protected[this] def saveError(value: Image): EngineException =
    new EngineException(s"Saving Image to database was unsuccessful : $value")

  override def save(image: Image): Future[Image] = {
    val query = BSONDocument("id" -> image.id)
    collection.flatMap { _
      .update(query, image, upsert = true)
      .flatMap { _ =>
        findOne(image.id)
          .flatMap {
            case Some(i) => Future.successful(i)
            case None    => Future.failed(saveError(image))
          }
      }
    }
  }

  override def findOne(id: String): Future[Option[Image]] = {
    log.debug("Request to get Image (ID) : {}", id)
    findOne("id" -> toBsonS(id))
  }

  def findOneByAssociate(id: String): Future[Option[Image]] = {
    log.debug("Request to get Image (ID) : {}", id)
    findOne("associateId" -> toBsonS(id))
  }

}
