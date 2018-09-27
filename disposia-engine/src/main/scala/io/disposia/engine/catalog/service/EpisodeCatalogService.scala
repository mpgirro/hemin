package io.disposia.engine.catalog.service

import javax.persistence.EntityManager
import akka.event.LoggingAdapter
import io.disposia.engine.catalog.mongo.EpisodeMongoRepository
import io.disposia.engine.catalog.repository.{EpisodeRepository, RepositoryFactoryBuilder}
import io.disposia.engine.domain.dto.{Episode, Podcast}
import io.disposia.engine.mapper.{EpisodeMapper, PodcastMapper, TeaserMapper}
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * @author Maximilian Irro
  */
@Repository
@Transactional
class EpisodeCatalogService(log: LoggingAdapter,
                            rfb: RepositoryFactoryBuilder,
                            db: DefaultDB)
                           (implicit ec: ExecutionContext)
    extends CatalogService {

    private var repositoryFactory: JpaRepositoryFactory = _
    private var episodeRepository: EpisodeRepository = _

    private val podcastMapper = PodcastMapper.INSTANCE
    private val episodeMapper = EpisodeMapper.INSTANCE
    private val teaserMapper = TeaserMapper.INSTANCE

    private val mongoRepo = new EpisodeMongoRepository(db, ec)

    override def refresh(em: EntityManager): Unit = {
        repositoryFactory = rfb.createRepositoryFactory(em)
        episodeRepository = repositoryFactory.getRepository(classOf[EpisodeRepository])
    }

    @Transactional
    def save(episodeDTO: Episode): Option[Episode] = {
        log.debug("Request to save Episode : {}", episodeDTO)
        mongoRepo
            .save(episodeDTO)
            .onComplete {
                case Success(e)  => log.debug("Saved to MongoDB : {}", e)
                case Failure(ex) =>
                    log.error("Saving to MongoDB failed : {}", ex)
                    ex.printStackTrace()
            }
        Option(episodeDTO)
          .map(e => episodeMapper.toEntity(e))
          .map(e => episodeRepository.save(e))
          .map(e => episodeMapper.toImmutable(e))
    }

    @Transactional(readOnly = true)
    def findOne(dbId: Long): Option[Episode] = {
        log.debug("Request to get Episode (ID) : {}", dbId)
        Option(dbId)
            .map(id => episodeRepository.findOne(id))
            .map(e => episodeMapper.toImmutable(e))
    }

    def find(exo: String): Future[Option[Episode]] = {
        log.debug("Request to get Episode (EXO) : {}", exo)
        mongoRepo.findOne(exo)
    }

    @Transactional(readOnly = true)
    def findOneByExo(episodeExo: String): Option[Episode] = {
        log.debug("Request to get Episode (EXO) : {}", episodeExo)
        Option(episodeExo)
          .map(exo => episodeRepository.findOneByExo(exo))
          .map(e => episodeMapper.toImmutable(e))
    }

    @Transactional(readOnly = true)
    def findAll(): List[Episode] = {
        log.debug("Request to get all Episodes")
        episodeRepository.findAll
            .asScala
            .map(e => episodeMapper.toImmutable(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findAllByPodcast(podcastDTO: Podcast): List[Episode] = {
        log.debug("Request to get all Episodes by Podcast : {}", podcastDTO)
        val podcast = podcastMapper.toEntity(podcastDTO)
        episodeRepository.findAllByPodcast(podcast)
            .asScala
            .map(e => episodeMapper.toImmutable(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findAllByPodcast(podcastExo: String): List[Episode] = {
        log.debug("Request to get all Episodes by Podcast (EXO) : {}", podcastExo)
        episodeRepository.findAllByPodcastExo(podcastExo)
            .asScala
            .map(e => episodeMapper.toImmutable(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findAllByPodcastAsTeaser(podcastExo: String): List[Episode] = {
        log.debug("Request to get all Episodes by Podcast (EXO) as teaser : {}", podcastExo)
        episodeRepository.findAllByPodcastExo(podcastExo)
            .asScala
            .map(e => teaserMapper.asTeaser(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findAllByPodcastAndGuid(podcastExo: String, guid: String): List[Episode] = {
        log.debug("Request to get all Episodes by Podcast (EXO) : {} and GUID : {}", podcastExo, guid)
        episodeRepository.findAllByPodcastAndGuid(podcastExo, guid)
            .asScala
            .map(e => teaserMapper.asTeaser(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findOneByEnclosure(enclosureUrl: String, enclosureLength: Long, enclosureType: String): Option[Episode] = {
        log.debug("Request to get Episode by enclosureUrl : '{}' and enclosureLength : {} and enclosureType : {}", enclosureUrl, enclosureLength, enclosureType)
        val result = episodeRepository.findOneByEnlosure(enclosureUrl, enclosureLength, enclosureType)
        Option(episodeMapper.toImmutable(result))
    }

    @Transactional(readOnly = true)
    def countAll(): Long = {
        log.debug("Request to count all Episodes")
        episodeRepository.countAll()
    }

}
