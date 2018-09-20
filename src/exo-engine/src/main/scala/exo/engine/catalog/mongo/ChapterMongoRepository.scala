package exo.engine.catalog.mongo

import exo.engine.catalog.mongo.BsonWrites.toBson
import exo.engine.domain.dto.{Chapter, ImmutableChapter}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class ChapterMongoRepository (db: DefaultDB)
                             (implicit ec: ExecutionContext) {

    private def collection: BSONCollection = db.collection("chapters")
    collection.create() // TODO ensure that the collection exists -> brauch ich das? mache ich nur weil Podcasts/Chapters gerade nicht geschrieben werden

    private implicit object ChapterReader extends BSONDocumentReader[Chapter] {
        override def read(bson: BSONDocument): Chapter = {
            val builder = ImmutableChapter.builder()
            val opt: Option[Chapter] = for {
                id <- bson.getAs[BSONNumberLike]("id").map(_.toLong) // TODO remove; no rel. DB
                episodeId <- bson.getAs[BSONNumberLike]("episodeId").map(_.toLong) // TODO remove; no rel. DB
                start <- bson.getAs[String]("start")
                title <- bson.getAs[String]("title")
                href <- bson.getAs[String]("href")
                image <- bson.getAs[String]("image")
                episodeExo <- bson.getAs[String]("episodeExo")
            } yield builder
                .setId(id)
                .setEpisodeId(episodeId)
                .setStart(start)
                .setTitle(title)
                .setHref(href)
                .setImage(image)
                .setEpisodeExo(episodeExo)
                .create()

            opt.get // the Chapter is required (or let throw an exception)
        }
    }

    private implicit object ChapterWriter extends BSONDocumentWriter[Chapter] {
        override def write(c: Chapter): BSONDocument =
            BSONDocument(
                "id" -> toBson(c.getId), // TODO remove; no rel. DB
                "episodeId" -> toBson(c.getEpisodeId), // TODO remove; no rel. DB
                "start" -> c.getStart,
                "title" -> c.getTitle,
                "href" -> c.getHref,
                "image"-> c.getImage,
                "episodeExo" -> c.getEpisodeExo
            )
    }

    // TODO this writes, but does not OVERWRITE existing chapter with same EXO!!
    def save(chapter: Chapter): Future[Unit] = {

        //println("Saving [MongoChapterService] : " + chapter.toString)

        /*
        collection.flatMap(_
            .insert[Chapter](ordered = false).one(chapter)
        ).map(_ => {})
        */

        /*
        collection
            .insert[Chapter](ordered = false)
            .one(chapter)
            .map(_ => {})
            */

        collection
            .insert[Chapter](ordered = false)
            .one(chapter)
            .map(_ => {})


        /*
        val writeRes: Future[WriteResult] =
            collection.insert[Chapter](ordered = false).one(chapter)

        writeRes.onComplete { // Dummy callbacks
            case Failure(e) => e.printStackTrace()
            case Success(writeResult) =>
                println(s"successfully inserted document with result: $writeResult")
        }

        writeRes.map(_ => {}) // in this example, do nothing with the success
        */
    }

    def findByExo(exo: String): Future[Option[Chapter]] = {
        val query = BSONDocument("exo" -> exo)
        collection
            .find(query)
            .one[Chapter]
    }

    def findAllByEpisodeExo(episodeExo: String): Future[List[Chapter]] = {
        val query = BSONDocument("episodeExo" -> episodeExo)
        collection
            .find(query)
            .cursor[Chapter]()
            .collect[List](-1, Cursor.FailOnError[List[Chapter]]())
    }

}
