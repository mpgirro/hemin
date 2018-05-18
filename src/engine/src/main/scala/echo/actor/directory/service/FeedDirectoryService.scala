package echo.actor.directory.service

import javax.persistence.EntityManager

import akka.event.LoggingAdapter
import echo.actor.directory.repository.{FeedRepository, RepositoryFactoryBuilder}
import echo.core.domain.dto.FeedDTO
import echo.core.mapper.FeedMapper
import org.springframework.data.domain.{PageRequest, Sort}
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
@Repository
@Transactional
class FeedDirectoryService (log: LoggingAdapter,
                            rfb: RepositoryFactoryBuilder) extends DirectoryService {

    private var repositoryFactory: JpaRepositoryFactory = _
    private var feedRepository: FeedRepository = _

    private val feedMapper = FeedMapper.INSTANCE

    override def refresh(em: EntityManager): Unit = {
        repositoryFactory = rfb.createRepositoryFactory(em)
        feedRepository = repositoryFactory.getRepository(classOf[FeedRepository])
    }

    @Transactional
    def save(feedDTO: FeedDTO): Option[FeedDTO] = {
        log.debug("Request to save Feed : {}", feedDTO)
        Option(feedDTO)
          .map(f => feedMapper.toEntity(f))
          .map(f => feedRepository.save(f))
          .map(f => feedMapper.toImmutable(f))
    }

    @Transactional
    def findOne(dbId: Long): Option[FeedDTO] = {
        log.debug("Request to get Feed (ID) : {}", dbId)
        Option(dbId)
          .map(id => feedRepository.findOne(id))
          .map(f => feedMapper.toImmutable(f))
    }

    @Transactional
    def findOneByExo(feedExo: String): Option[FeedDTO] = {
        log.debug("Request to get Feed (EXO) : {}", feedExo)
        Option(feedExo)
          .map(exo => feedRepository.findOneByExo(exo))
          .map(f => feedMapper.toImmutable(f))
    }

    @Transactional
    def findAll(page: Int, size: Int): List[FeedDTO] = {
        log.debug("Request to get all Feeds by page : {} and size : {}", page, size)
        //val sort = new Sort(new Sort.Order(Direction.ASC, "registration_timestamp"))
        //val pageable = new PageRequest(page, size, sort)
        val pageable = new PageRequest(page, size)
        feedRepository.findAll(pageable)
            .asScala
            .map(f => feedMapper.toImmutable(f))
            .toList
    }

    @Transactional
    def findAllByUrl(url: String): List[FeedDTO] = {
        log.debug("Request to get all Feeds by URL : {}", url)
        feedRepository.findAllByUrl(url)
            .asScala
            .map(f => feedMapper.toImmutable(f))
            .toList
    }

    @Transactional
    def findOneByUrlAndPodcastExo(url: String, podcastExo: String): Option[FeedDTO] = {
        log.debug("Request to get all Feeds by URL : {} and Podcast (EXO) : {}", url, podcastExo)
        val result = feedRepository.findOneByUrlAndPodcastExo(url, podcastExo)
        Option(feedMapper.toImmutable(result))
    }

    @Transactional
    def findAllByPodcast(podcastExo: String): List[FeedDTO] = {
        log.debug("Request to get all Feeds by Podcast (EXO) : {}", podcastExo)
        feedRepository.findAllByPodcast(podcastExo)
            .asScala
            .map(f => feedMapper.toImmutable(f))
            .toList
    }

    @Transactional
    def countAll(): Long = {
        log.debug("Request to count all Feeds")
        feedRepository.countAll()
    }

}
