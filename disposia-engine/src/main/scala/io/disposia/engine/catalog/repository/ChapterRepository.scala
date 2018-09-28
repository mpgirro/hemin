package io.disposia.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.disposia.engine.catalog.repository.BsonConversion.{toBson, toDocument}
import io.disposia.engine.domain.Chapter
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

@Deprecated
class ChapterRepository(db: DefaultDB, ec: ExecutionContext)
  extends MongoRepository[Chapter] {

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] implicit def bsonWriter: BSONDocumentWriter[Chapter] = BsonConversion.ChapterWriter

  override protected[this] implicit def bsonReader: BSONDocumentReader[Chapter] = BsonConversion.ChapterReader

  override def collection: BSONCollection = db.apply("chapters")

  override protected[this] def log: Logger = Logger(getClass)

  // TODO this writes, but does not OVERWRITE existing chapter with same EXO!!
  def save(chapter: Chapter): Future[Unit] = {
    collection
      .insert[Chapter](ordered = false)
      .one(chapter)
      .map(_ => {})
  }

  def findByExo(exo: String): Future[Option[Chapter]] = {
    log.debug("Request to get Chapter (EXO) : {}", exo)
    val query = toDocument(Map("exo" -> toBson(exo)))
    findOne(query)
  }

  def findAllByEpisode(episodeExo: String): Future[List[Chapter]] = {
    log.debug("Request to get all Chapters by Episode (EXO) : {}", episodeExo)
    val query = toDocument(Map("episodeExo" -> toBson(episodeExo)))
    findAll(query)
  }

}
