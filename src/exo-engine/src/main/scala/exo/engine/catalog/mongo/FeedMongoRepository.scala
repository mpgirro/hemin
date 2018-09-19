package exo.engine.catalog.mongo

import java.time.LocalDateTime

import exo.engine.domain.FeedStatus
import exo.engine.domain.dto.{Feed, ImmutableFeed}
import exo.engine.mapper.DateMapper
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONLong, BSONNumberLike}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * @author max
  */
class FeedMongoRepository (db: DefaultDB)
                          (implicit ec: ExecutionContext) {

    //private def collection: BSONCollection = db.collection("feeds")
    private def collection: BSONCollection = db.collection("feeds")

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

            opt.get // the person is required (or let throw an exception)
        }
    }

    private implicit object FeedWriter extends BSONDocumentWriter[Feed] {
        override def write(feed: Feed): BSONDocument =
            BSONDocument(
                "id" -> BSONLong(feed.getId), // TODO remove; no rel. DB
                "podcastId" -> BSONLong(feed.getPodcastId), // TODO remove; no rel. DB
                "exo" -> feed.getExo,
                "podcastExo" -> feed.getPodcastExo,
                "url" -> feed.getUrl,
                "lastChecked" ->  BSONDateTime(DateMapper.INSTANCE.asMilliseconds(feed.getLastChecked)),
                "lastStatus"-> feed.getLastStatus.getName,
                "registrationTimestamp" -> BSONDateTime(DateMapper.INSTANCE.asMilliseconds(feed.getRegistrationTimestamp))
            )
    }

    def save(feed: Feed): Future[Unit] = {
        //println("Saving [MongoFeedService] : " + feed.toString)
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
        /*
        collection
            .find(query)
            .one[Feed]
            */
        collection
            .find(query)
            .one[Feed]
    }

    def findAllByEpisodeExo(podcastExo: String): Future[List[Feed]] = {
        val query = BSONDocument("podcastExo" -> podcastExo)
        /*
        collection
            .find(query)
            .cursor[Feed]()
            .collect[List](-1, Cursor.FailOnError[List[Feed]]()) // -1 to get all matches
            */
        collection
            .find(query)
            .cursor[Feed]()
            .collect[List](-1, Cursor.FailOnError[List[Feed]]()) // -1 to get all matches

    }

}
