package exo.engine.catalog.service

import javax.persistence.EntityManager

import akka.event.LoggingAdapter
import exo.engine.catalog.repository.{EpisodeRepository, RepositoryFactoryBuilder}
import exo.engine.domain.dto.{EpisodeDTO, PodcastDTO}
import exo.engine.mapper.{EpisodeMapper, PodcastMapper, TeaserMapper}
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
@Repository
@Transactional
class EpisodeCatalogService(log: LoggingAdapter,
                            rfb: RepositoryFactoryBuilder) extends CatalogService {

    private var repositoryFactory: JpaRepositoryFactory = _
    private var episodeRepository: EpisodeRepository = _

    private val podcastMapper = PodcastMapper.INSTANCE
    private val episodeMapper = EpisodeMapper.INSTANCE
    private val teaserMapper = TeaserMapper.INSTANCE

    override def refresh(em: EntityManager): Unit = {
        repositoryFactory = rfb.createRepositoryFactory(em)
        episodeRepository = repositoryFactory.getRepository(classOf[EpisodeRepository])
    }

    @Transactional
    def save(episodeDTO: EpisodeDTO): Option[EpisodeDTO] = {
        log.debug("Request to save Episode : {}", episodeDTO)
        Option(episodeDTO)
          .map(e => episodeMapper.toEntity(e))
          .map(e => episodeRepository.save(e))
          .map(e => episodeMapper.toImmutable(e))
    }

    @Transactional(readOnly = true)
    def findOne(dbId: Long): Option[EpisodeDTO] = {
        log.debug("Request to get Episode (ID) : {}", dbId)
        Option(dbId)
            .map(id => episodeRepository.findOne(id))
            .map(e => episodeMapper.toImmutable(e))
    }

    @Transactional(readOnly = true)
    def findOneByExo(episodeExo: String): Option[EpisodeDTO] = {
        log.debug("Request to get Episode (EXO) : {}", episodeExo)
        Option(episodeExo)
          .map(exo => episodeRepository.findOneByExo(exo))
          .map(e => episodeMapper.toImmutable(e))
    }

    @Transactional(readOnly = true)
    def findAll(): List[EpisodeDTO] = {
        log.debug("Request to get all Episodes")
        episodeRepository.findAll
            .asScala
            .map(e => episodeMapper.toImmutable(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findAllByPodcast(podcastDTO: PodcastDTO): List[EpisodeDTO] = {
        log.debug("Request to get all Episodes by Podcast : {}", podcastDTO)
        val podcast = podcastMapper.toEntity(podcastDTO)
        episodeRepository.findAllByPodcast(podcast)
            .asScala
            .map(e => episodeMapper.toImmutable(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findAllByPodcast(podcastExo: String): List[EpisodeDTO] = {
        log.debug("Request to get all Episodes by Podcast (EXO) : {}", podcastExo)
        episodeRepository.findAllByPodcastExo(podcastExo)
            .asScala
            .map(e => episodeMapper.toImmutable(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findAllByPodcastAsTeaser(podcastExo: String): List[EpisodeDTO] = {
        log.debug("Request to get all Episodes by Podcast (EXO) as teaser : {}", podcastExo)
        episodeRepository.findAllByPodcastExo(podcastExo)
            .asScala
            .map(e => teaserMapper.asTeaser(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findAllByPodcastAndGuid(podcastExo: String, guid: String): List[EpisodeDTO] = {
        log.debug("Request to get all Episodes by Podcast (EXO) : {} and GUID : {}", podcastExo, guid)
        episodeRepository.findAllByPodcastAndGuid(podcastExo, guid)
            .asScala
            .map(e => teaserMapper.asTeaser(e))
            .toList
    }

    @Transactional(readOnly = true)
    def findOneByEnclosure(enclosureUrl: String, enclosureLength: Long, enclosureType: String): Option[EpisodeDTO] = {
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
