package io.hemin.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import reactivemongo.api.{Cursor, QueryOpts}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import scala.concurrent.{ExecutionContext, Future}

trait MongoRepository[T] {

  protected[this] implicit def executionContext: ExecutionContext
  protected[this] implicit def bsonWriter: BSONDocumentWriter[T]
  protected[this] implicit def bsonReader: BSONDocumentReader[T]

  protected[this] def log: Logger

  protected[this] def collection: Future[BSONCollection]

  /**
    * Save entity to database collection
    *
    * @param t save entity to database
    * @return the entity as it was written to the database collection, eventually
    */
  def save(t: T): Future[T]

  /**
    * Find one entity by ID
    *
    * @param id the ID of the entity
    * @return  the entity eventually, or None if not found
    */
  def findOne(id: String): Future[Option[T]]

  /**
    * Find one entity by ID, if the Option contains an ID
    *
    * @param optId the ID of the entity wrapped in a Future
    * @return the entity eventually, or None if not found
    */
  def findOne(optId: Option[String]): Future[Option[T]] = optId match {
    case Some(id) => findOne(id)
    case None     => Future { None }
  }

  /**
    * Drops the collection of this repository
    *
    * @return true if drop was successful
    */
  def drop: Future[Boolean] = {
    collection.flatMap { c =>
      log.debug("Dropping collection : {}", c.name)
      c.drop(failIfNotFound = true) }
  }

  /**
    * Find one by example
    *
    * @param example The example object
    * @return The object matching the given example
    */
  def findOne(example: T): Future[Option[T]] = findOne(bsonWriter.write(example))

  /**
    * Find many by example
    *
    * @param example The example object
    * @return List of objects matching the given example
    */
  def findAll(example: T): Future[List[T]] = findAll(bsonWriter.write(example))

  protected[this] def findOne(query: BSONDocument): Future[Option[T]] =
    collection.flatMap { _
      .find(query)
      .one[T]
      .recover {
        case ex: Throwable =>
          log.error("Error on findOne({}) : {}", query, ex)
          None
      }
    }

  /**
    *
    * @param page The page of the result frames to retrieve
    * @param size The size of the frame to retrieve
    * @return The results within the window (page*size, size)
    */
  def findAll(page: Int, size: Int): Future[List[T]] = {
    log.debug("Request to get all by page : {} and size : {}", page, size)
    if (page < 1 || size < 1) {
      log.warn("Window parameters are too small (page = {}, size = {})", page, size)
      Future { List() }
    } else {
      val query = BSONDocument()
      findAll(query, page, size)
    }
  }

  protected[this] def findAll(query: BSONDocument): Future[List[T]] =
    collection.flatMap { _
      .find(query)
      .cursor[T]()
      .collect[List](-1, Cursor.FailOnError[List[T]]())
      .recover {
        case ex: Throwable =>
          log.error("Error on findAll({}) : {}", query, ex)
          List()
      }
    }

  // TODO does not seem to work!
  protected[this] def findAll(query: BSONDocument, page: Int, size: Int): Future[List[T]] =
    collection.flatMap { _
      .find(query)
      .options(QueryOpts(page * size, size)) // TODO start, pageSize --> (page*size, size)
      .cursor[T]()
      .collect[List](-1, Cursor.FailOnError[List[T]]())
      .recover {
        case ex: Throwable =>
          log.error("Error on findAll({}) : {}", query, ex)
          List()
      }
    }

}