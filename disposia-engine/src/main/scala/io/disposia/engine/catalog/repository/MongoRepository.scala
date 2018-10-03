package io.disposia.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import reactivemongo.api.{Cursor, QueryOpts}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import scala.concurrent.{ExecutionContext, Future}

trait MongoRepository[T] {

  protected[this] implicit def executionContext: ExecutionContext
  protected[this] implicit def bsonWriter: BSONDocumentWriter[T]
  protected[this] implicit def bsonReader: BSONDocumentReader[T]

  protected[this] def collection: BSONCollection

  protected[this] def log: Logger

  def drop: Future[Boolean] = {
    log.debug("Dropping collection : {}", collection.name)
    collection.drop(failIfNotFound = true)
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
    collection
      .find(query)
      .one[T]
      .recover {
        case ex: Throwable =>
          log.error("Error on findOne({}) : {}", query, ex)
          None
      }

  protected[this] def findAll(query: BSONDocument): Future[List[T]] =
    collection
      .find(query)
      .cursor[T]()
      .collect[List](-1, Cursor.FailOnError[List[T]]())
      .recover {
        case ex: Throwable =>
          log.error("Error on findAll({}) : {}", query, ex)
          List()
      }

  // TODO untested!
  protected[this] def findAll(query: BSONDocument, page: Int, size: Int): Future[List[T]] =
    collection
      .find(query)
      .options(QueryOpts(page*size, size)) // TODO start, pageSize --> (page*size, size)
      .cursor[T]()
      .collect[List](-1, Cursor.FailOnError[List[T]]())
      .recover {
        case ex: Throwable =>
          log.error("Error on findAll({}) : {}", query, ex)
          List()
      }

}
