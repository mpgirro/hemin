package io.disposia.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.disposia.engine.catalog.repository.BsonConversion.{toBson, toDocument}
import io.disposia.engine.domain.Feed
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class FeedRepository(db: DefaultDB, ec: ExecutionContext)
  extends MongoRepository[Feed] {

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] implicit def bsonWriter: BSONDocumentWriter[Feed] = BsonConversion.FeedWriter

  override protected[this] implicit def bsonReader: BSONDocumentReader[Feed] = BsonConversion.FeedReader

  override protected[this] def collection: BSONCollection = db.apply("feeds")

  override protected[this] def log: Logger = Logger(getClass)

  def save(feed: Feed): Future[Feed] = {
    val query = BSONDocument("id" -> feed.getId)
    collection
      .update(query, feed, upsert = true)
      .flatMap { _ => findOne(feed.getId)
        .map {
          case Some(f) => f
          case None    => throw new RuntimeException("Saving Feed to database was unsuccessful : " + feed)
        }
      }
  }

  def findOne(id: String): Future[Option[Feed]] = {
    log.debug("Request to get Feed (ID) : {}", id)
    val query = toDocument(Map("id" -> toBson(id)))
    findOne(query)
  }

  def findOneByUrlAndPodcastId(url: String, podcastId: String): Future[Option[Feed]] = {
    log.debug("Request to get all Feeds by URL : {} and Podcast (ID) : {}", url, podcastId)
    val query = toDocument(Map(
      "url" -> toBson(url),
      "podcastId" -> toBson(podcastId))
    )
    findOne(query)
  }

  def findAllByPodcast(podcastId: String): Future[List[Feed]] = {
    val query = toDocument(Map("podcastId" -> toBson(podcastId)))
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
