package exo.engine.catalog.mongo

import exo.engine.domain.dto.Podcast
import reactivemongo.api.{Cursor, DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.{BSONDocumentWriter, BSONDocumentReader, Macros, document}

import scala.concurrent.{ ExecutionContext, Future }

/**
  * @author max
  */
object MongoRepo {

    /*
    val dbName = "exodb"

    // My settings (see available connection options)
    val mongoUri = s"mongodb://localhost:27017/$dbName?authMode=scram-sha1"

    // TODO pass the actors EX
    import ExecutionContext.Implicits.global // use any appropriate context


    // Connect to the database: Must be done only once per application
    val driver = MongoDriver()
    val parsedUri = MongoConnection.parseURI(mongoUri)
    val connection = parsedUri.map(driver.connection(_))

    // Database and collections: Get references
    val futureConnection = Future.fromTry(connection)
    def db: Future[DefaultDB] = futureConnection.flatMap(_.database(dbName))
    def podcastCollection = db.map(_.collection("podcast"))

    // Write Documents: insert or update

    implicit def podcastWriter: BSONDocumentWriter[Podcast] = Macros.writer[Podcast]
    // or provide a custom one

    def createPerson(podcast: Podcast): Future[Unit] =
        podcastCollection.flatMap(_.insert(podcast).map(_ => {})) // use podcastWriter

    def updatePerson(podcast: Podcast): Future[Int] = {
        val selector = document(
            "firstName" -> podcast.firstName,
            "lastName" -> podcast.lastName
        )

        // Update the matching person
        podcastCollection.flatMap(_.update(selector, podcast).map(_.n))
    }

    implicit def personReader: BSONDocumentReader[Podcast] = Macros.reader[Podcast]
    // or provide a custom one

    def findPodcastByExp(exo: String): Future[List[Podcast]] =
        podcastCollection.flatMap(_.find(document("exo" -> exo)) // query builder
            .cursor[Podcast]() // using the result cursor
            .collect[List](-1, Cursor.FailOnError[List[Podcast]]()))
    // ... deserializes the document using personReader
    */
}
