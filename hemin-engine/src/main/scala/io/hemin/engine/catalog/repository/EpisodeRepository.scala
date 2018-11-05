package io.hemin.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.hemin.engine.catalog.repository.BsonConversion._
import io.hemin.engine.model.Episode
import io.hemin.engine.util.Errors
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class EpisodeRepository(db: Future[DefaultDB], ec: ExecutionContext)
  extends MongoRepository[Episode] {

  override protected[this] val log: Logger = Logger(getClass)

  override protected[this] implicit val executionContext: ExecutionContext = ec

  override protected[this] implicit val bsonWriter: BSONDocumentWriter[Episode] = BsonConversion.episodeWriter

  override protected[this] implicit val bsonReader: BSONDocumentReader[Episode] = BsonConversion.episodeReader

  override protected[this] val sort: BSONDocument = BSONDocument("_id" -> 1) // sort ascending by mongo ID

  override protected[this] def collection: Future[BSONCollection] = db.map(_.collection("episodes"))

  override def save(episode: Episode): Future[Episode] = {
    val query = BSONDocument("id" -> episode.id)
    collection.flatMap { _
      .update(query, episode, upsert = true)
      .flatMap { _ =>
        findOne(episode.id)
          .flatMap {
            case Some(e) => Future.successful(e)
            case None    => Future.failed(Errors.mongoErrorSaveEpisode(episode))
          }
      }
    }
  }

  override def findOne(id: String): Future[Option[Episode]] = {
    log.debug("Request to get Episode (ID) : {}", id)
    findOne("id" -> toBsonS(id))
  }

  def findAllByPodcast(podcastId: String): Future[List[Episode]] = {
    log.debug("Request to get all Episodes by Episode (ID) : {}", podcastId)
    findAll("podcastId" -> toBsonS(podcastId))
  }

  def findAllByPodcastAndGuid(podcastId: String, guid: String): Future[List[Episode]] = {
    log.debug("Request to get all Episodes by Podcast (ID) : {} and GUID : {}", podcastId, guid)
    findAll(
      "podcastId" -> toBsonS(podcastId),
      "guid"      -> toBsonS(guid))
  }

  def findOneByEnclosure(enclosureUrl: String, enclosureLength: Long, enclosureType: String): Future[List[Episode]] =
    findOneByEnclosure(Option(enclosureUrl), Option(enclosureLength), Option(enclosureType))

  def findOneByEnclosure(enclosureUrl: Option[String], enclosureLength: Option[Long], enclosureType: Option[String]): Future[List[Episode]] = {
    log.debug("Request to get Episode by enclosureUrl : '{}' and enclosureLength : {} and enclosureType : {}", enclosureUrl, enclosureLength, enclosureType)
    findAll(
      "enclosureUrl"    -> toBsonS(enclosureUrl),
      "enclosureLength" -> toBsonL(enclosureLength),
      "enclosureType"   -> toBsonS(enclosureType)
    )
  }

}
