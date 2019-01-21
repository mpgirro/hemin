package io.hemin.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.hemin.engine.HeminException
import io.hemin.engine.catalog.repository.BsonConversion._
import io.hemin.engine.model.Episode
import io.hemin.engine.util.TimeUtil
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class EpisodeRepository(db: Future[DefaultDB],
                        ec: ExecutionContext)
  extends MongoRepository[Episode] {

  override protected[this] val log: Logger = Logger(getClass)

  override protected[this] implicit val executionContext: ExecutionContext = ec

  override protected[this] implicit val bsonWriter: BSONDocumentWriter[Episode] = BsonConversion.episodeWriter

  override protected[this] implicit val bsonReader: BSONDocumentReader[Episode] = BsonConversion.episodeReader

  override protected[this] val defaultSort: BSONDocument = BSONDocument("_id" -> 1) // sort ascending by mongo ID

  override protected[this] def querySafeguard: BSONDocument = BSONDocument("pubDate" -> BSONDocument("$lt" -> TimeUtil.now))

  override protected[this] def collection: Future[BSONCollection] = db.map(_.collection("episodes"))

  override protected[this] def saveError(value: Episode): HeminException =
    new HeminException(s"Saving Episode to database was unsuccessful : $value")

  override def save(episode: Episode): Future[Episode] = {
    val query = BSONDocument("id" -> episode.id)
    collection.flatMap { _
      .update(query, episode, upsert = true)
      .flatMap { _ =>
        findOne(episode.id)
          .flatMap {
            case Some(e) => Future.successful(e)
            case None    => Future.failed(saveError(episode))
          }
      }
    }
  }

  override def findOne(id: String): Future[Option[Episode]] = {
    log.debug("Request to get Episode (ID) : {}", id)
    findOne(Query("id" -> toBsonS(id)))
  }

  def findAllByPodcast(podcastId: String): Future[List[Episode]] = {
    log.debug("Request to get all Episodes by Episode (ID) : {}", podcastId)
    findAll(Query("podcastId" -> toBsonS(podcastId)))
  }

  def findAllByPodcastAndGuid(podcastId: String, guid: String): Future[List[Episode]] = {
    log.debug("Request to get all Episodes by Podcast (ID) : {} and GUID : {}", podcastId, guid)
    findAll(Query(
      "podcastId" -> toBsonS(podcastId),
      "guid"      -> toBsonS(guid)
    ))
  }

  def findOneByEnclosure(enclosureUrl: String, enclosureLength: Long, enclosureType: String): Future[List[Episode]] =
    findOneByEnclosure(Option(enclosureUrl), Option(enclosureLength), Option(enclosureType))

  def findOneByEnclosure(enclosureUrl: Option[String], enclosureLength: Option[Long], enclosureType: Option[String]): Future[List[Episode]] = {
    log.debug("Request to get Episode by enclosure.url : '{}' and enclosure.length : {} and enclosure.typ : {}", enclosureUrl, enclosureLength, enclosureType)
    findAll(Query(
      "enclosure.url"    -> toBsonS(enclosureUrl),
      "enclosure.length" -> toBsonL(enclosureLength),
      "enclosure.typ"    -> toBsonS(enclosureType)
    ))
  }

  /** Finds all Episodes by the reference they currently hold to an image. Depending
    * on their current processing state, this reference is either the URL of the image
    * file, or the ID of the already processed image in our database.
    *
    * @param image Reference as String to an image (URL, ID)
    * @return All Episodes holding the reference to the Image
    */
  def findAllByImage(image: String): Future[List[Episode]] = {
    log.debug("Request to get all Episodes where image is : {}", image)
    findAll(Query("image" -> toBsonS(image)))
  }

  /** Find podcasts in the window (pageNumber,pageSize) where registration is complete,
    * sorted descending by
    *
    * @param pageNumber
    * @param pageSize
    * @return
    */
  def findLatest(pageNumber: Int, pageSize: Int): Future[List[Episode]] = {
    log.debug("Request to get latest Episodes by pageNumber : {} and pageSize : {}", pageNumber, pageSize)

    val query = Query()
    val sort = BSONDocument("pubDate" -> -1)

    findAll(query, pageNumber, pageSize, sort)
  }

}
