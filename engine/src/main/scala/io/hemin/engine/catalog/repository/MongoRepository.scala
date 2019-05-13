package io.hemin.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.hemin.engine.HeminException
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, DefaultDB, ReadPreference}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONValue}

import scala.concurrent.{ExecutionContext, Future}

object MongoRepository {
  val sortAscendingByMongoId: BSONDocument = BSONDocument("id" -> 1)
}

trait MongoRepository[T] {

  protected[this] implicit val executionContext: ExecutionContext

  protected[this] implicit val bsonWriter: BSONDocumentWriter[T]

  protected[this] implicit val bsonReader: BSONDocumentReader[T]

  protected[this] val collectionName: String

  protected[this] val log: Logger

  protected[this] val database: Future[DefaultDB]

  protected[this] val defaultSort: BSONDocument

  protected[this] def querySafeguard: BSONDocument

  protected[this] def saveError(value: T): HeminException

  /** Save entity to database collection
    *
    * @param t save entity to database
    * @return the entity as it was written to the database collection, eventually
    */
  def save(t: T): Future[T]

  /** Find one entity by ID
    *
    * @param id the ID of the entity
    * @return  the entity eventually, or None if not found
    */
  def findOne(id: String): Future[Option[T]]

  /** Find one entity by ID, if the Option contains an ID
    *
    * @param optId the ID of the entity wrapped in a Future
    * @return the entity eventually, or None if not found
    */
  def findOne(optId: Option[String]): Future[Option[T]] = optId match {
    case Some(id) => findOne(id)
    case None     => Future.successful(None)
  }

  /** Drops the collection of this repository
    *
    * @return true if drop was successful
    */
  def deleteAll(): Future[Boolean] = {
    collection.flatMap { c =>
      log.debug("Dropping database collection : {}", c.name)
      c.drop(failIfNotFound = true)
    }
  }

  def countDocuments: Future[Int] = collection.flatMap(_.count(None))

  /** Find one by example
    *
    * @param example The example object
    * @return The object matching the given example
    */
  def findOne(example: T): Future[Option[T]] = findOne(bsonWriter.write(example))

  /** Find many by example
    *
    * @param example The example object
    * @return List of objects matching the given example
    */
  def findAll(example: T): Future[List[T]] = findAll(bsonWriter.write(example))

  protected[this] def findOne(selectors: (String, Option[BSONValue])*): Future[Option[T]] = findOne(Query(selectors.toMap))

  protected[this] def findOne(query: BSONDocument): Future[Option[T]] =
    collection.flatMap { _
      .find(query)
      .one[T]
      .recover {
        case ex: Exception =>
          log.error("Error on findOne('{}') : {}", query, ex)
          None
      }
    }

  /**
    *
    * @param pageNumber The page of the result frames to retrieve
    * @param pageSize The size of the frame to retrieve
    * @return The results within the window (page*size, size)
    */
  def findAll(pageNumber: Int, pageSize: Int): Future[List[T]] =
    if (pageNumber < 1 || pageSize < 1) {
      log.warn("Window parameters are too small (pageNumber = {}, pageSize = {})", pageNumber, pageSize)
      Future.successful(Nil)
    } else {
      val query = BSONDocument()
      findAll(query, pageNumber, pageSize)
    }

  protected[this] def collection: Future[BSONCollection] =
    database.map(_.collection(collectionName))

  protected[this] def findAll(selectors: (String, Option[BSONValue])*): Future[List[T]] =
    findAll(Query(selectors.toMap))

  protected[this] def findAll(query: BSONDocument): Future[List[T]] =
    findAll(query, defaultSort)

  protected[this] def findAll(query: BSONDocument, sort: BSONDocument): Future[List[T]] =
    findAll(query, pageNumber = 1, pageSize = -1, defaultSort)

  protected[this] def findAll(query: BSONDocument, pageNumber: Int, pageSize: Int): Future[List[T]] =
    findAll(query, pageNumber, pageSize, defaultSort)

  protected[this] def findAll(query: BSONDocument, pageNumber: Int, pageSize: Int, sort: BSONDocument): Future[List[T]] =
    collection.flatMap { _
      .find(querySafeguard.merge(query))
      .sort(sort)
      .skip((pageNumber-1) * pageSize)
      .cursor[T](ReadPreference.primaryPreferred)
      .collect[List](pageSize, Cursor.FailOnError[List[T]]())
      .recover {
        case ex: Exception =>
          log.error("Error on findAll(query = '{}', pageNumber = {}, pageSize = {}, sort = {}) : {}", query, pageNumber, pageSize, sort, ex)
          Nil
      }
    }
}
