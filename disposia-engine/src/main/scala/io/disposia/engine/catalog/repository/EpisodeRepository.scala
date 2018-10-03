package io.disposia.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.disposia.engine.catalog.repository.BsonConversion.{toBson, toDocument}
import io.disposia.engine.domain.Episode
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
    val query = BSONDocument("id" -> episode.getId)
    collection
      .update(query, episode, upsert = true)
      .flatMap { _ => findOne(episode.getId)
        .map {
          case Some(e) => e
          case None    => throw new RuntimeException("Saving Episode to database was unsuccessful : " + episode)
        }
      }
  }

  def findOne(id: String): Future[Option[Episode]] = {
    log.debug("Request to get Episode (ID) : {}", id)
    val query = toDocument(Map("id" -> toBson(id)))
    findOne(query)
  }

  def findAllByPodcast(podcastId: String): Future[List[Episode]] = {
    log.debug("Request to get all Episodes by Podcast (ID) : {}", podcastId)
    val query = toDocument(Map("podcastId" -> toBson(podcastId)))
    findAll(query)
  }

  def findAll(page: Int, size: Int): Future[List[Episode]] = {
    log.debug("Request to get all Episodes by page : {} and size : {}", page, size)
    val query = BSONDocument()
    findAll(query, page, size)
  }

  def findAllByPodcastAndGuid(podcastId: String, guid: String): Future[List[Episode]] = {
    log.debug("Request to get all Episodes by Podcast (ID) : {} and GUID : {}", podcastId, guid)
    val query = toDocument(Map(
      "podcastId" -> toBson(podcastId),
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
