package io.hemin.engine.catalog.repository

import com.typesafe.scalalogging.Logger
import io.hemin.engine.catalog.CatalogConfig
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class RepositoryFactory(config: CatalogConfig,
                        ec: ExecutionContext) {

  private implicit val implicitExecutionContext: ExecutionContext = ec

  private val log: Logger = Logger(getClass)

  private lazy val (driver, connection, databaseName) = {
    val driver = MongoDriver()

    //registerDriverShutdownHook(driver)

    (for {
      parsedUri <- MongoConnection.parseURI(config.mongoUri)
      con <- driver.connection(parsedUri, strictUri = true)
      db <- parsedUri.db match {
        case Some(dbName) => Success(dbName)
        case _            => Failure[String](new IllegalArgumentException(
          s"cannot resolve connection from URI: $parsedUri"
        ))
      }
    } yield (driver, con, db)).get
  }

  private lazy val lnm = s"${connection.supervisor}/${connection.name}"

  @inline private def resolveDB: Future[DefaultDB] =
    connection.database(databaseName).andThen {
      case _ => log.debug(s"[$lnm] MongoDB resolved: $databaseName")
    }

  def close(): Unit = {
    log.debug("Stopping the MongoDriver...")
    driver.close()
  }

  /** Drop all database collections.
    * __Very destructive__!
    */
  def dropAll(): Unit = {
    getPodcastRepository.deleteAll()
    getEpisodeRepository.deleteAll()
    getFeedRepository.deleteAll()
    getImageRepository.deleteAll()
  }

  def getPodcastRepository: PodcastRepository = new PodcastRepository(resolveDB, ec)

  def getEpisodeRepository: EpisodeRepository = new EpisodeRepository(resolveDB, ec)

  def getFeedRepository: FeedRepository = new FeedRepository(resolveDB, ec)

  def getImageRepository: ImageRepository = new ImageRepository(resolveDB, ec)

}
