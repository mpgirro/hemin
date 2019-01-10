package io.hemin.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.hemin.engine.EngineException
import io.hemin.engine.catalog.repository.BsonConversion._
import io.hemin.engine.model.Podcast
import io.hemin.engine.util.TimeUtil
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

  override protected[this] val defaultSort: BSONDocument = BSONDocument("_id" -> 1) // sort ascending by mongo ID

  override protected[this] def querySafeguard: BSONDocument = BSONDocument("pubDate" -> BSONDocument("$lt" -> TimeUtil.now))

  override protected[this] def collection: Future[BSONCollection] = db.map(_.collection("podcasts"))

  override protected[this] def saveError(value: Podcast): EngineException =
    new EngineException(s"Saving Podcast to database was unsuccessful : $value")

  override def save(podcast: Podcast): Future[Podcast] = {
    val query = BSONDocument("id" -> podcast.id)
    collection.flatMap { _
      .update(query, podcast, upsert = true)
      .flatMap { _ => findOne(podcast.id)
        .flatMap {
          case Some(p) => Future.successful(p)
          case None    => Future.failed(saveError(podcast))
        }
      }
    }
  }

  override def findOne(id: String): Future[Option[Podcast]] = {
    log.debug("Request to get Podcast (ID) : {}", id)
    findOne("id" -> toBsonS(id))
  }

  def findAllRegistrationCompleteAsTeaser(pageNumber: Int, pageSize: Int): Future[List[Podcast]] = {
    log.debug("Request to get all Podcasts where registration is complete by pageNumber : {} and pageSize : {}", pageNumber, pageSize)
    findAll(Query("registration.complete" -> toBsonB(true)), pageNumber, pageSize)
  }

  /** Finds all Podcasts by the reference they currently hold to an image. Depending
    * on their current processing state, this reference is either the URL of the image
    * file, or the ID of the already processed image in our database.
    *
    * @param image Reference as String to an image (URL, ID)
    * @return All Podcasts holding the reference to the Image
    */
  def findAllByImage(image: String): Future[List[Podcast]] = {
    log.debug("Request to get all Podcasts where image is : {}", image)
    findAll(Query("image" -> toBsonS(image)))
  }

  /** Find podcasts in the window (pageNumber,pageSize) where registration is complete,
    * sorted descending by
    *
    * @param pageNumber
    * @param pageSize
    * @return
    */
  def findNewest(pageNumber: Int, pageSize: Int): Future[List[Podcast]] = {
    log.debug("Request to get newest Podcasts where registration is complete by pageNumber : {} and pageSize : {}", pageNumber, pageSize)

    val query = Query("registration.complete" -> toBsonB(true))
    val sort = BSONDocument("registration.timestamp" -> -1)

    findAll(query, pageNumber, pageSize, sort)
  }

  def distinctItunesCategories: Future[Set[String]] = {
    log.debug("Request to get all Categories")
    collection.flatMap(_.distinct[String, Set]("itunes.categories"))
  }

}
