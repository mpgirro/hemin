package io.disposia.engine.catalog.mongo

import io.disposia.engine.domain.dto.Episode
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author max
  */
class EpisodeMongoRepository (collection: BSONCollection)
                             (implicit ec: ExecutionContext) {

    /*
    private def collection: BSONCollection = db.collection("episodes")
    collection.create() // TODO ensure that the collection exists -> brauch ich das? mache ich nur weil Podcasts/Chapters gerade nicht geschrieben werden
    */

    /*
    def collection(name: String)(implicit ec: ExecutionContext): BSONCollection =
        db(ec).apply(name)
        */

    private implicit val episodeWriter: BsonConversion.EpisodeWriter.type = BsonConversion.EpisodeWriter
    private implicit val episodeReader: BsonConversion.EpisodeReader.type = BsonConversion.EpisodeReader

    def save(episode: Episode): Future[Unit] = {
        /*
        collection
            .insert[Episode](ordered = false)
            .one(episode)
            .map(_ => {})
            */
        val query = BSONDocument("exo" -> episode.getExo)
        collection
            .update(query, episode, upsert = true)
            .map {
                case ok if ok.ok   => println("episode saved successfully")
                case error         => println("ERROR on saving episode : " + error.errmsg)
            }
    }

    def findByExo(exo: String): Future[Option[Episode]] = {
        val query = BSONDocument("exo" -> exo)
        collection
            .find(query)
            .one[Episode]
    }

}
