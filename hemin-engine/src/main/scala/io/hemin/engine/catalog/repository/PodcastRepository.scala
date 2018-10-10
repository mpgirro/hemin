package io.hemin.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.hemin.engine.catalog.repository.BsonConversion._
import io.hemin.engine.domain.Podcast
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class PodcastRepository(db: Future[DefaultDB], ec: ExecutionContext)
  extends MongoRepository[Podcast] {

  override protected[this] def log: Logger = Logger(getClass)

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] implicit def bsonWriter: BSONDocumentWriter[Podcast] = BsonConversion.podcastWriter

  override protected[this] implicit def bsonReader: BSONDocumentReader[Podcast] = BsonConversion.podcastReader

  override protected[this] def collection: Future[BSONCollection] = db.map(_.collection("podcasts"))

  override def save(podcast: Podcast): Future[Podcast] = {
    val query = BSONDocument("id" -> podcast.id)
    collection.flatMap { _
      .update(query, podcast, upsert = true)
      .flatMap { _ => findOne(podcast.id)
        .map {
          case Some(p) => p
          case None => throw new RuntimeException("Saving Podcast to database was unsuccessful : " + podcast)
        }
      }
    }
  }

  override def findOne(id: String): Future[Option[Podcast]] = {
    log.debug("Request to get Podcast (ID) : {}", id)
    val query = toDocument(Map("id" -> toBsonS(id)))
    findOne(query)
  }

  def findAllRegistrationCompleteAsTeaser(page: Int, size: Int): Future[List[Podcast]] = {
    log.debug("Request to get all Podcasts where registration is complete by page : {} and size : {}", page, size)
    val query = toDocument(Map("registrationComplete" -> toBsonB(true)))
    findAll(query, page, size)
  }

}
