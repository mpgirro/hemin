package io.disposia.engine.catalog.mongo

import io.disposia.engine.domain.dto.Feed
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class FeedMongoRepository (collection: BSONCollection)
                          (implicit ec: ExecutionContext) {

    /*
    private def collection: BSONCollection = db.collection("feeds")
    collection.create() // TODO ensure that the collection exists -> brauch ich das? mache ich nur weil Podcasts/Chapters gerade nicht geschrieben werden
    */

    private implicit val feedWriter: BsonConversion.FeedWriter.type = BsonConversion.FeedWriter
    private implicit val feedReader: BsonConversion.FeedReader.type = BsonConversion.FeedReader

    def save(feed: Feed): Future[Option[Feed]] = {
        /*
        collection
            .insert[Feed](ordered = false)
            .one(feed)
            .map(_ => {})
            */
        println("Writing Feed DTO to mongodb collection : " + collection.name)
        val query = BSONDocument("exo" -> feed.getExo)
        collection
            .update(query, feed, upsert = true)
            .flatMap { _ =>
                findByExo(feed.getExo) }
        /*
        val writeRes: Future[WriteResult] =
            collection.insert[BSONDocument](ordered = false).one(document)
        */

        /*
        val document = FeedWriter.write(feed)
        val writeRes: Future[WriteResult] = collection
                .insert[BSONDocument](ordered = false)
                .one(document)

        writeRes.onComplete { // Dummy callbacks
            case Failure(e) => e.printStackTrace()
            case Success(writeResult) =>
                println(s"successfully inserted Feed with result: $writeResult")
        }

        writeRes.map(_ => {}) // in this example, do nothing with the success
        */
    }

    def findByExo(exo: String): Future[Option[Feed]] = {
        val query = BSONDocument("exo" -> exo)
        collection
            .find(query)
            .one[Feed]
    }

    def findAllByEpisodeExo(podcastExo: String): Future[List[Feed]] = {
        val query = BSONDocument("podcastExo" -> podcastExo)
        collection
            .find(query)
            .cursor[Feed]()
            .collect[List](-1, Cursor.FailOnError[List[Feed]]()) // -1 to get all matches
    }

}
