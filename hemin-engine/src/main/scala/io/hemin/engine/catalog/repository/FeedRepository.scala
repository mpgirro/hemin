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

  override protected[this] val log: Logger = Logger(getClass)

  override protected[this] implicit val executionContext: ExecutionContext = ec

  override protected[this] implicit val bsonWriter: BSONDocumentWriter[Feed] = BsonConversion.feedWriter

  override protected[this] implicit val bsonReader: BSONDocumentReader[Feed] = BsonConversion.feedReader

  override protected[this] val sort: BSONDocument = BSONDocument("_id" -> 1) // sort ascending by mongo ID

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
    findOne("id" -> toBsonS(id))
  }

  def findOneByUrlAndPodcastId(url: String, podcastId: String): Future[Option[Feed]] = {
    log.debug("Request to get all Feeds by URL : {} and Podcast (ID) : {}", url, podcastId)
    findOne(Query(
      "url"       -> toBsonS(url),
      "podcastId" -> toBsonS(podcastId))
    )
  }

  def findAllByPodcast(podcastId: String): Future[List[Feed]] = {
    findAll(Query("podcastId" -> toBsonS(podcastId)))
  }

  def findAllByUrl(url: String): Future[List[Feed]] = {
    log.debug("Request to get all Feeds by URL : {}", url)
    findAll(Query("url" -> toBsonS(url)))
  }

}
