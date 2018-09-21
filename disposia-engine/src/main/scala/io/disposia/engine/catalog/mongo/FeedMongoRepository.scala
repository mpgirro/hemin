package io.disposia.engine.catalog.mongo

import io.disposia.engine.catalog.mongo.BsonWrites.toBson
import io.disposia.engine.domain.FeedStatus
import io.disposia.engine.domain.dto.{Feed, ImmutableFeed}
import io.disposia.engine.mapper.DateMapper
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class FeedMongoRepository (db: DefaultDB)
                          (implicit ec: ExecutionContext) {

    private def collection: BSONCollection = db.collection("feeds")
    collection.create() // TODO ensure that the collection exists -> brauch ich das? mache ich nur weil Podcasts/Chapters gerade nicht geschrieben werden

    private implicit object FeedReader extends BSONDocumentReader[Feed] {
        override def read(bson: BSONDocument): Feed = {
            val builder = ImmutableFeed.builder()
            val opt: Option[Feed] = for {
                id <- bson.getAs[BSONNumberLike]("id").map(_.toLong) // TODO remove; no rel. DB
                podcastId <- bson.getAs[BSONNumberLike]("podcastId").map(_.toLong) // TODO remove; no rel. DB
                exo <- bson.getAs[String]("exo")
                podcastExo <- bson.getAs[String]("podcastExo")
                url <- bson.getAs[String]("url")
                lastChecked <- bson.getAs[BSONDateTime]("lastChecked").map(dt => DateMapper.INSTANCE.asLocalDateTime(dt.value))
                lastStatus <- bson.getAs[String]("lastStatus")
                registrationTimestamp <- bson.getAs[BSONDateTime]("registrationTimestamp").map(dt => DateMapper.INSTANCE.asLocalDateTime(dt.value))
            } yield builder
                .setId(id)
                .setPodcastId(podcastId)
                .setExo(exo)
                .setUrl(url)
                .setLastChecked(lastChecked)
                .setLastStatus(FeedStatus.getByName(lastStatus))
                .setRegistrationTimestamp(registrationTimestamp)
                .create()

            opt.get // the Feed is required (or let throw an exception)
        }
    }

    private implicit object FeedWriter extends BSONDocumentWriter[Feed] {
        override def write(f: Feed): BSONDocument =
            BSONDocument(
                "id" -> toBson(f.getId), // TODO remove; no rel. DB
                "podcastId" -> toBson(f.getPodcastId), // TODO remove; no rel. DB
                "exo" -> f.getExo,
                "podcastExo" -> f.getPodcastExo,
                "url" -> f.getUrl,
                "lastChecked" ->  toBson(f.getLastChecked),
                "lastStatus"-> f.getLastStatus.getName,
                "registrationTimestamp" -> toBson(f.getRegistrationTimestamp)
            )
    }

    def save(feed: Feed): Future[Unit] = {
        collection
            .insert[Feed](ordered = false)
            .one(feed)
            .map(_ => {})
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
