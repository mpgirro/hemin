package io.disposia.engine.catalog.service

import javax.persistence.EntityManager
import akka.event.LoggingAdapter
import akka.util.Timeout
import io.disposia.engine.catalog.mongo.{ChapterMongoRepository, EpisodeMongoRepository}
import io.disposia.engine.catalog.repository.{ChapterRepository, RepositoryFactoryBuilder}
import io.disposia.engine.domain.dto.{Chapter, ModifiableChapter}
import io.disposia.engine.mapper.ChapterMapper
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.transaction.annotation.Transactional
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.collection.JavaConverters._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * @author Maximilian Irro
  */
class ChapterCatalogService(log: LoggingAdapter,
                            rfb: RepositoryFactoryBuilder,
                            db: DefaultDB)
                           (implicit ec: ExecutionContext)
    extends CatalogService {

    private var repositoryFactory: JpaRepositoryFactory = _
    private var chapterRepository: ChapterRepository = _

    private val chapterMapper = ChapterMapper.INSTANCE

    private val mongoRepo = new ChapterMongoRepository(db, ec)

    override def refresh(em: EntityManager): Unit = {
        repositoryFactory = rfb.createRepositoryFactory(em)
        chapterRepository = repositoryFactory.getRepository(classOf[ChapterRepository])
    }

    /*
    // TODO experimental
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
    val futureConnection: Future[MongoConnection] = Future.fromTry(connection)
    //def db: Future[DefaultDB] = futureConnection.flatMap(_.database(dbName))
    val mongoConnection: MongoConnection = Await.result(futureConnection, 10.seconds)
    val db: DefaultDB = Await.result(mongoConnection.database(dbName), 10.seconds)

    val mongoService = new MongoChapterService(db)

    // - - - - - - - - -
    */

    @Transactional
    def save(chapterDTO: Chapter): Option[Chapter] = {
        log.debug("Request to save Chapter : {}", chapterDTO)
        mongoRepo
            .save(chapterDTO)

        Option(chapterDTO)
          .map(c => chapterMapper.toEntity(c))
          .map(c => chapterRepository.save(c))
          .map(c => chapterMapper.toModifiable(c))
          .map(_.toImmutable)
    }

    @Transactional
    def saveAll(episodeId: Long, chapters: java.util.List[Chapter]): Unit = {
        /*
        log.debug("Request to save Chapters for Episode (ID) : {}", episodeId)
        for(capter <- chapters.asScala){
            val c = new ModifiableChapter().from(capter)
            c.setEpisodeId(episodeId)
            save(c)
        }
        */
        for(capter <- chapters.asScala){
            save(capter)
        }
    }

    @Transactional
    def findAllByEpisode(episodeExo: String): List[Chapter] = {
        /*
        log.debug("Request to get all Chapters by Episode (EXO) : {}", episodeExo)
        chapterRepository.findAllByEpisodeExo(episodeExo)
            .asScala
            .map(c => chapterMapper.toModifiable(c).toImmutable)
            .toList
            */
        val f = mongoRepo.findAllByEpisode(episodeExo)
        Await.result(f, 10.seconds)
    }

}
