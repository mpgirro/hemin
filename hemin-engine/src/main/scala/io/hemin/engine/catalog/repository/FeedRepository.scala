package io.hemin.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.hemin.engine.catalog.repository.BsonConversion._
import io.hemin.engine.domain.Feed
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class FeedRepository(db: Future[DefaultDB], ec: ExecutionContext)
  extends MongoRepository[Feed] {

  override protected[this] def log: Logger = Logger(getClass)

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] implicit def bsonWriter: BSONDocumentWriter[Feed] = BsonConversion.feedWriter

  override protected[this] implicit def bsonReader: BSONDocumentReader[Feed] = BsonConversion.feedReader

  override protected[this] def collection: Future[BSONCollection] = db.map(_.collection("feeds"))

  override def save(feed: Feed): Future[Feed] = {
    val query = BSONDocument("id" -> feed.id)
    collection.flatMap { _
      .update(query, feed, upsert = true)
      .flatMap { _ =>
        findOne(feed.id)
          .map {
            case Some(f) => f
            case None => throw new RuntimeException("Saving Feed to database was unsuccessful : " + feed)
          }
      }
    }
  }

  override def findOne(id: String): Future[Option[Feed]] = {
    log.debug("Request to get Feed (ID) : {}", id)
    val query = toDocument(Map("id" -> toBsonS(id)))
    findOne(query)
  }

  def findOneByUrlAndPodcastId(url: String, podcastId: String): Future[Option[Feed]] = {
    log.debug("Request to get all Feeds by URL : {} and Podcast (ID) : {}", url, podcastId)
    val query = toDocument(Map(
      "url"       -> toBsonS(url),
      "podcastId" -> toBsonS(podcastId))
    )
    findOne(query)
  }

  def findAllByPodcast(podcastId: String): Future[List[Feed]] = {
    val query = toDocument(Map("podcastId" -> toBsonS(podcastId)))
    findAll(query)
  }

  def findAllByUrl(url: String): Future[List[Feed]] = {
    log.debug("Request to get all Feeds by URL : {}", url)
    val query = toDocument(Map("url" -> toBsonS(url)))
    findAll(query)
  }

}