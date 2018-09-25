package io.disposia.engine.catalog.mongo

import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

import scala.concurrent.{ExecutionContext, Future}

trait MongoRepository[T] {

    protected[this] implicit def executionContext: ExecutionContext
    protected[this] implicit def bsonWriter: BSONDocumentWriter[T]
    protected[this] implicit def bsonReader: BSONDocumentReader[T]

    protected[this] def collection(): BSONCollection

    protected[this] def findOneByQuery(query: BSONDocument): Future[Option[T]] =
        collection()
            .find(query)
            .one[T]

    protected[this] def findAllByQuery(query: BSONDocument): Future[List[T]] =
        collection()
            .find(query)
            .cursor[T]()
            .collect[List](-1, Cursor.FailOnError[List[T]]())

}
