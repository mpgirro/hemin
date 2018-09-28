package io.disposia.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.disposia.engine.domain.dto.Podcast
import io.disposia.engine.catalog.repository.BsonConversion.{toBson,toDocument}
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class PodcastRepository(db: DefaultDB, ec: ExecutionContext)
  extends MongoRepository[Podcast] {

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] implicit def bsonWriter: BSONDocumentWriter[Podcast] = BsonConversion.PodcastWriter

  override protected[this] implicit def bsonReader: BSONDocumentReader[Podcast] = BsonConversion.PodcastReader

  override protected[this] def collection: BSONCollection = db.apply("podcasts")

  override protected[this] def log: Logger = Logger(getClass)

  def save(podcast: Podcast): Future[Podcast] = {
    /*
    collection
        .insert[Podcast](ordered = false)
        .one(podcast)
        .map(e => {
            if (!e.ok)
                println("ERROR on saving podcast : " + e.writeErrors)
        })
     */
    //println("Writing Podcast DTO to mongodb collection : " + collection.name)
    val query = BSONDocument("exo" -> podcast.getExo)
    collection
      .update(query, podcast, upsert = true)
      .flatMap { _ => findOne(podcast.getExo)
          .map {
            case Some(p) => p
            case None => throw new RuntimeException("Saving Podcast to database was unsuccessful : " + podcast)
          }
      }

  }

  def findOne(exo: String): Future[Option[Podcast]] = {
    log.debug("Request to get Podcast (EXO) : {}", exo)
    val query = toDocument(Map("exo" -> toBson(exo)))
    findOne(query)
  }

  def findAll(page: Int, size: Int): Future[List[Podcast]] = {
    log.debug("Request to get all Podcasts by page : {} and size : {}", page, size)
    val query = BSONDocument()
    findAll(query, page, size)
  }

  def findAllRegistrationCompleteAsTeaser(page: Int, size: Int): Future[List[Podcast]] = {
    log.debug("Request to get all Podcasts where registration is complete by page : {} and size : {}", page, size)
    val query = toDocument(Map("registrationComplete" -> toBson(true)))
    findAll(query, page, size)
  }

}
