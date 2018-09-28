package io.disposia.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.disposia.engine.catalog.repository.BsonConversion.{toBson, toDocument}
import io.disposia.engine.domain.dto.Episode
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class EpisodeRepository(db: DefaultDB, ec: ExecutionContext)
  extends MongoRepository[Episode] {

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] implicit def bsonWriter: BSONDocumentWriter[Episode] = BsonConversion.EpisodeWriter

  override protected[this] implicit def bsonReader: BSONDocumentReader[Episode] = BsonConversion.EpisodeReader

  override protected[this] def collection: BSONCollection = db.apply("episodes")

  override protected[this] def log: Logger = Logger(getClass)

  def save(episode: Episode): Future[Episode] = {
    /*
    collection
        .insert[Episode](ordered = false)
        .one(episode)
        .map(_ => {})
        */
    //println("Writing Episode DTO to mongodb collection : " + collection.name)
    val query = BSONDocument("exo" -> episode.getExo)
    collection
      .update(query, episode, upsert = true)
      .flatMap { _ => findOne(episode.getExo)
        .map {
          case Some(e) => e
          case None    => throw new RuntimeException("Saving Episode to database was unsuccessful : " + episode)
        }
      }
  }

  def findOne(exo: String): Future[Option[Episode]] = {
    log.debug("Request to get Episode (EXO) : {}", exo)
    val query = toDocument(Map("exo" -> toBson(exo)))
    findOne(query)
  }

  def findAllByPodcast(podcastExo: String): Future[List[Episode]] = {
    log.debug("Request to get all Episodes by Podcast (EXO) : {}", podcastExo)
    val query = toDocument(Map("podcastExo" -> toBson(podcastExo)))
    findAll(query)
  }

  def findAll(page: Int, size: Int): Future[List[Episode]] = {
    log.debug("Request to get all Episodes by page : {} and size : {}", page, size)
    val query = BSONDocument()
    findAll(query, page, size)
  }

  def findAllByPodcastAndGuid(podcastExo: String, guid: String): Future[List[Episode]] = {
    log.debug("Request to get all Episodes by Podcast (EXO) : {} and GUID : {}", podcastExo, guid)
    val query = toDocument(Map(
      "podcastExo" -> toBson(podcastExo),
      "guid"       -> toBson(guid)
    ))
    findAll(query)
  }

  def findOneByEnclosure(enclosureUrl: String, enclosureLength: Long, enclosureType: String): Future[List[Episode]] = {
    log.debug("Request to get Episode by enclosureUrl : '{}' and enclosureLength : {} and enclosureType : {}", enclosureUrl, enclosureLength, enclosureType)
    val query = toDocument(Map(
      "enclosureUrl"    -> toBson(enclosureUrl),
      "enclosureLength" -> toBson(enclosureLength),
      "enclosureType"   -> toBson(enclosureType),
    ))
    findAll(query)
  }

}
