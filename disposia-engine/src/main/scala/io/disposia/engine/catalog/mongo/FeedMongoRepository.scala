package io.disposia.engine.catalog.mongo

import com.typesafe.scalalogging.Logger
import io.disposia.engine.catalog.mongo.BsonConversion.{toBson, toDocument}
import io.disposia.engine.domain.dto.Feed
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class FeedMongoRepository (db: DefaultDB, ec: ExecutionContext)
  extends MongoRepository[Feed] {

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] implicit def bsonWriter: BSONDocumentWriter[Feed] = BsonConversion.FeedWriter

  override protected[this] implicit def bsonReader: BSONDocumentReader[Feed] = BsonConversion.FeedReader

  override protected[this] def collection: BSONCollection = db.apply("feeds")

  override protected[this] def log: Logger = Logger(classOf[FeedMongoRepository])

  def save(feed: Feed): Future[Feed] = {
    /*
    collection
        .insert[Feed](ordered = false)
        .one(feed)
        .map(_ => {})
        */
    //println("Writing Feed DTO to mongodb collection : " + collection.name)
    val query = BSONDocument("exo" -> feed.getExo)
    collection
      .update(query, feed, upsert = true)
      .flatMap { _ => findOne(feed.getExo)
        .map {
          case Some(f) => f
          case None    => throw new RuntimeException("Saving Feed to database was unsuccessful : " + feed)
        }
      }
    /*
    val writeRes: Future[WriteResult] =
        collection.insert[BSONDocument](ordered = false).one(document)
    */

    /*
    val document = FeedWriter.write(feed)
    val writeRes: Future[WriteResult] = collection
            .insert[BSONDocument](ordered = false)
            .one(document)

    writeRes.onComplete { // Dummy callbacks
        case Failure(e) => e.printStackTrace()
        case Success(writeResult) =>
            println(s"successfully inserted Feed with result: $writeResult")
    }

    writeRes.map(_ => {}) // in this example, do nothing with the success
    */
  }

  def findOne(exo: String): Future[Option[Feed]] = {
    log.debug("Request to get Feed (EXO) : {}", exo)
    val query = toDocument(Map("exo" -> toBson(exo)))
    findOne(query)
  }

  def findOneByUrlAndPodcastExo(url: String, podcastExo: String): Future[Option[Feed]] = {
    log.debug("Request to get all Feeds by URL : {} and Podcast (EXO) : {}", url, podcastExo)
    val query = toDocument(Map(
      "url" -> toBson(url),
      "podcastExo" -> toBson(podcastExo))
    )
    findOne(query)
  }

  def findAllByPodcast(podcastExo: String): Future[List[Feed]] = {
    val query = toDocument(Map("podcastExo" -> toBson(podcastExo)))
    findAll(query)
  }

  def findAll(page: Int, size: Int): Future[List[Feed]] = {
    log.debug("Request to get all Feeds by page : {} and size : {}", page, size)
    val query = BSONDocument()
    findAll(query, page, size)
  }

  def findAllByUrl(url: String): Future[List[Feed]] = {
    log.debug("Request to get all Feeds by URL : {}", url)
    val query = toDocument(Map("url" -> toBson(url)))
    findAll(query)
  }

}
