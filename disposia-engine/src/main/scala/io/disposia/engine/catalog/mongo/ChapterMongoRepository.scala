package io.disposia.engine.catalog.mongo

import com.typesafe.scalalogging.Logger
import io.disposia.engine.catalog.mongo.BsonConversion.{toBson, toDocument}
import io.disposia.engine.domain.dto.Chapter
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

@Deprecated
class ChapterMongoRepository (db: DefaultDB, ec: ExecutionContext)
  extends MongoRepository[Chapter] {

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] implicit def bsonWriter: BSONDocumentWriter[Chapter] = BsonConversion.ChapterWriter

  override protected[this] implicit def bsonReader: BSONDocumentReader[Chapter] = BsonConversion.ChapterReader

  override def collection: BSONCollection = db.apply("chapters")

  override protected[this] def log: Logger = Logger(classOf[ChapterMongoRepository])

  // TODO this writes, but does not OVERWRITE existing chapter with same EXO!!
  def save(chapter: Chapter): Future[Unit] = {

    //println("Saving [MongoChapterService] : " + chapter.toString)

    /*
    collection.flatMap(_
        .insert[Chapter](ordered = false).one(chapter)
    ).map(_ => {})
    */

    /*
    collection
        .insert[Chapter](ordered = false)
        .one(chapter)
        .map(_ => {})
        */
    //println("Writing Chapter DTO to mongodb collection : " + collection.name)
    collection
      .insert[Chapter](ordered = false)
      .one(chapter)
      .map(_ => {})


    /*
    val writeRes: Future[WriteResult] =
        collection.insert[Chapter](ordered = false).one(chapter)

    writeRes.onComplete { // Dummy callbacks
        case Failure(e) => e.printStackTrace()
        case Success(writeResult) =>
            println(s"successfully inserted document with result: $writeResult")
    }

    writeRes.map(_ => {}) // in this example, do nothing with the success
    */
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
