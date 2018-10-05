package io.disposia.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.disposia.engine.catalog.repository.BsonConversion._
import io.disposia.engine.newdomain.NewEpisode
import io.disposia.engine.olddomain.OldEpisode
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class EpisodeRepository(db: DefaultDB, ec: ExecutionContext)
  extends MongoRepository[NewEpisode] {

  override protected[this] def log: Logger = Logger(getClass)

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] implicit def bsonWriter: BSONDocumentWriter[NewEpisode] = NewEpisode.bsonWriter

  override protected[this] implicit def bsonReader: BSONDocumentReader[NewEpisode] = NewEpisode.bsonReader

  override protected[this] def collection: BSONCollection = db.apply("episodes")

  override def save(episode: NewEpisode): Future[NewEpisode] = {
    val query = BSONDocument("id" -> episode.id)
    collection
      .update(query, episode, upsert = true)
      .flatMap { _ => findOne(episode.id)
        .map {
          case Some(e) => e
          case None    => throw new RuntimeException("Saving Episode to database was unsuccessful : " + episode)
        }
      }
  }

  override def findOne(id: String): Future[Option[NewEpisode]] = {
    log.debug("Request to get Episode (ID) : {}", id)
    val query = toDocument(Map("id" -> toBsonS(id)))
    findOne(query)
  }

  def findAllByPodcast(podcastId: String): Future[List[NewEpisode]] = {
    log.debug("Request to get all Episodes by Episode (ID) : {}", podcastId)
    val query = toDocument(Map("podcastId" -> toBsonS(podcastId)))
    findAll(query)
  }

  def findAll(page: Int, size: Int): Future[List[NewEpisode]] = {
    log.debug("Request to get all Episodes by page : {} and size : {}", page, size)
    val query = BSONDocument()
    findAll(query, page, size)
  }

  def findAllByPodcastAndGuid(podcastId: String, guid: String): Future[List[NewEpisode]] = {
    log.debug("Request to get all Episodes by Podcast (ID) : {} and GUID : {}", podcastId, guid)
    val query = toDocument(Map(
      "podcastId" -> toBsonS(podcastId),
      "guid"      -> toBsonS(guid)
    ))
    findAll(query)
  }

  def findOneByEnclosure(enclosureUrl: String, enclosureLength: Long, enclosureType: String): Future[List[NewEpisode]] =
    findOneByEnclosure(Option(enclosureUrl), Option(enclosureLength), Option(enclosureType))

  def findOneByEnclosure(enclosureUrl: Option[String], enclosureLength: Option[Long], enclosureType: Option[String]): Future[List[NewEpisode]] = {
    log.debug("Request to get Episode by enclosureUrl : '{}' and enclosureLength : {} and enclosureType : {}", enclosureUrl, enclosureLength, enclosureType)
    val query = toDocument(Map(
      "enclosureUrl"    -> toBsonS(enclosureUrl),
      "enclosureLength" -> toBsonL(enclosureLength),
      "enclosureType"   -> toBsonS(enclosureType),
    ))
    findAll(query)
  }

}
