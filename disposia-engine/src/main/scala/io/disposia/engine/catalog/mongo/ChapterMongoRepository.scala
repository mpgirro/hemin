package io.disposia.engine.catalog.mongo

import io.disposia.engine.domain.dto.Chapter
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

class ChapterMongoRepository (collection: BSONCollection)
                             (implicit ec: ExecutionContext) {

    /*
    private def collection: BSONCollection = db.collection("chapters")
    collection.create() // TODO ensure that the collection exists -> brauch ich das? mache ich nur weil Podcasts/Chapters gerade nicht geschrieben werden
    */

    private implicit val chapterWriter: BsonConversion.ChapterWriter.type = BsonConversion.ChapterWriter
    private implicit val chapterReader: BsonConversion.ChapterReader.type = BsonConversion.ChapterReader


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
        //println("Writing Chapter DTO to mongodb collection : " + collection.name)
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
