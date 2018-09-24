package io.disposia.engine.catalog.mongo

import io.disposia.engine.domain.dto.Podcast
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class PodcastMongoRepository (collection: BSONCollection)
                             (implicit ec: ExecutionContext) {

    /*
    private def collection: BSONCollection = db.collection("podcasts")
    collection.create() // TODO ensure that the collection exists -> brauch ich das? mache ich nur weil Podcasts/Chapters gerade nicht geschrieben werden
    */

    private implicit val podcastWriter: BsonConversion.PodcastWriter.type = BsonConversion.PodcastWriter
    private implicit val podcastReader: BsonConversion.PodcastReader.type = BsonConversion.PodcastReader

    def save(podcast: Podcast): Future[Unit] = {
        /*
        collection
            .insert[Podcast](ordered = false)
            .one(podcast)
            .map(e => {
                if (!e.ok)
                    println("ERROR on saving podcast : " + e.writeErrors)
            })
         */
        println("Writing podcast to mongodb : " + podcast.getExo)
        val query = BSONDocument("exo" -> podcast.getExo)
        collection
            .update(query, podcast, upsert = true)
            .map {
                case ok if ok.ok   => println("podcast saved successfully")
                case error         => println("ERROR on saving podcast : " + error.errmsg)
            }

    }

    def findByExo(exo: String): Future[Option[Podcast]] = {
        val query = BSONDocument("exo" -> exo)
        collection
            .find(query)
            .one[Podcast]
    }

}
