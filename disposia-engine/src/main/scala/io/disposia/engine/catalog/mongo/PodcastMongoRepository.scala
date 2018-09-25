package io.disposia.engine.catalog.mongo

import com.typesafe.scalalogging.Logger
import io.disposia.engine.domain.dto.Podcast
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class PodcastMongoRepository (db: DefaultDB, ec: ExecutionContext)
    extends MongoRepository[Podcast] {

    private val log = Logger(classOf[PodcastMongoRepository])

    override protected[this] implicit def executionContext: ExecutionContext = ec

    override protected[this] implicit def bsonWriter: BSONDocumentWriter[Podcast] = BsonConversion.PodcastWriter

    override protected[this] implicit def bsonReader: BSONDocumentReader[Podcast] = BsonConversion.PodcastReader

    override protected[this] def collection(): BSONCollection = db.apply("podcasts")

    def save(podcast: Podcast): Future[Option[Podcast]] = {
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
        collection()
            .update(query, podcast, upsert = true)
            .flatMap { _ => findOne(podcast.getExo) }

    }

    def findOne(exo: String): Future[Option[Podcast]] = {
        log.debug("Request to get Podcast (EXO) : {}", exo)
        val query = BSONDocument("exo" -> exo)
        findOneByQuery(query)
    }

}
