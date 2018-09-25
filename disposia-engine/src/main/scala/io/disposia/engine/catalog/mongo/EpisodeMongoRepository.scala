package io.disposia.engine.catalog.mongo

import com.typesafe.scalalogging.Logger
import io.disposia.engine.domain.dto.Episode
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author max
  */
class EpisodeMongoRepository (db: DefaultDB, ec: ExecutionContext)
    extends MongoRepository[Episode] {

    private val log = Logger(classOf[EpisodeMongoRepository])

    override protected[this] implicit def executionContext: ExecutionContext = ec

    override protected[this] implicit def bsonWriter: BSONDocumentWriter[Episode] = BsonConversion.EpisodeWriter

    override protected[this] implicit def bsonReader: BSONDocumentReader[Episode] = BsonConversion.EpisodeReader

    override protected[this] def collection(): BSONCollection = db.apply("episodes")

    def save(episode: Episode): Future[Option[Episode]] = {
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
            .flatMap { _ =>
                findOne(episode.getExo) }
    }

    def findOne(exo: String): Future[Option[Episode]] = {
        log.debug("Request to get Episode (EXO) : {}", exo)
        val query = BSONDocument("exo" -> exo)
        findOneByQuery(query)
    }

    def findAllByPodcast(podcastExo: String): Future[List[Episode]] = {
        log.debug("Request to get all Episodes by Podcast (EXO) : {}", podcastExo)
        val query = BSONDocument("podcastExo" -> podcastExo)
        findAllByQuery(query)
    }

    def findAllByPodcastAndGuid(podcastExo: String, guid: String): Future[List[Episode]] = {
        log.debug("Request to get all Episodes by Podcast (EXO) : {} and GUID : {}", podcastExo, guid)
        val query = BSONDocument(
            "podcastExo" -> podcastExo,
            "guid" -> guid
        )
        findAllByQuery(query)
    }

    def findOneByEnclosure(enclosureUrl: String, enclosureLength: Long, enclosureType: String): Future[List[Episode]] = {
        log.debug("Request to get Episode by enclosureUrl : '{}' and enclosureLength : {} and enclosureType : {}", enclosureUrl, enclosureLength, enclosureType)
        val query = BSONDocument(
            "enclosureUrl"    -> enclosureUrl,
            "enclosureLength" -> enclosureLength,
            "enclosureType"   -> enclosureType
        )
        findAllByQuery(query)
    }

}
