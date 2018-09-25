package io.disposia.engine.catalog.mongo

import com.typesafe.scalalogging.Logger
import io.disposia.engine.domain.dto.Feed
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class FeedMongoRepository (db: DefaultDB, ec: ExecutionContext)
    extends MongoRepository[Feed] {

    private val log = Logger(classOf[FeedMongoRepository])

    override protected[this] implicit def executionContext: ExecutionContext = ec

    override protected[this] implicit def bsonWriter: BSONDocumentWriter[Feed] = BsonConversion.FeedWriter

    override protected[this] implicit def bsonReader: BSONDocumentReader[Feed] = BsonConversion.FeedReader

    override protected[this] def collection(): BSONCollection = db.apply("feeds")

    def save(feed: Feed): Future[Option[Feed]] = {
        /*
        collection
            .insert[Feed](ordered = false)
            .one(feed)
            .map(_ => {})
            */
        //println("Writing Feed DTO to mongodb collection : " + collection.name)
        val query = BSONDocument("exo" -> feed.getExo)
        collection()
            .update(query, feed, upsert = true)
            .flatMap { _ =>
                findOne(feed.getExo) }
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

    def findOne(exo: String): Future[Option[Feed]] = {
        log.debug("Request to get Feed (EXO) : {}", exo)
        val query = BSONDocument("exo" -> exo)
        findOneByQuery(query)
    }

    def findAllByPodcast(podcastExo: String): Future[List[Feed]] = {
        val query = BSONDocument("podcastExo" -> podcastExo)
        findAllByQuery(query)
    }

}
