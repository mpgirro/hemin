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

  override protected[this] val log: Logger = Logger(getClass)

  override protected[this] implicit val executionContext: ExecutionContext = ec

  override protected[this] implicit val bsonWriter: BSONDocumentWriter[Podcast] = BsonConversion.podcastWriter

  override protected[this] implicit val bsonReader: BSONDocumentReader[Podcast] = BsonConversion.podcastReader

  override protected[this] val sort: BSONDocument = BSONDocument("_id" -> 1) // sort ascending by mongo ID

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
    findOne("id" -> toBsonS(id))
  }

  def findAllRegistrationCompleteAsTeaser(page: Int, size: Int): Future[List[Podcast]] = {
    log.debug("Request to get all Podcasts where registration is complete by page : {} and size : {}", page, size)
    findAll(Query("registrationComplete" -> toBsonB(true)), page, size)
  }

}
