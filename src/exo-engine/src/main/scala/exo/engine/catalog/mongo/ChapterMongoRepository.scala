package exo.engine.catalog.mongo

import exo.engine.domain.dto.{Chapter, ImmutableChapter}
import exo.engine.exception.EchoException
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONLong, BSONNumberLike, Macros}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * @author max
  */
class ChapterMongoRepository (db: DefaultDB)
                             (implicit ec: ExecutionContext) {

    // TODO pass the actors EX
    //import ExecutionContext.Implicits.global // use any appropriate context

    //private val collection: BSONCollection = db("chapters")

    private def collection: BSONCollection = db.collection("chapters")
    //private def collection: BSONCollection = db.collection("chapters")

    //private implicit def writer: BSONDocumentWriter[Chapter] = Macros.writer[Chapter]
    // or provide a custom one

    //private implicit def reader: BSONDocumentReader[Chapter] = Macros.reader[Chapter]
    // or provide a custom one

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

            opt.get // the person is required (or let throw an exception)
        }
    }

    private implicit object ChapterWriter extends BSONDocumentWriter[Chapter] {
        override def write(chapter: Chapter): BSONDocument =
            BSONDocument(
                "id" -> BSONLong(chapter.getId), // TODO remove; no rel. DB
                "episodeId" -> BSONLong(chapter.getEpisodeId), // TODO remove; no rel. DB
                "start" -> chapter.getStart,
                "title" -> chapter.getTitle,
                "href" -> chapter.getHref,
                "image"-> chapter.getImage,
                "episodeExo" -> chapter.getEpisodeExo
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
        // run this query over the collection
        //collection.find(query).one[Chapter]

        collection
            .find(query).one[Chapter]

        //collection.find(query).one[Chapter]
    }

    def findAllByEpisodeExo(episodeExo: String): Future[List[Chapter]] = {
        val query = BSONDocument("episodeExo" -> episodeExo)
        /*
        collection.find(query).cursor[Chapter]()
            .collect[List](-1, // -1 to get all matches
            Cursor.FailOnError[List[Chapter]]())
            */

        collection
            .find(query)
            .cursor[Chapter]()
            .collect[List](-1, Cursor.FailOnError[List[Chapter]]())


        /*
        collection
            .find(query).cursor[Chapter]()
            .collect[List](-1, Cursor.FailOnError[List[Chapter]]()) // -1 to get all matches
        */
    }

}
