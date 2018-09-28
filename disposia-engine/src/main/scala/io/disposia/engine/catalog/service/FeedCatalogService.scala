package io.disposia.engine.catalog.service

import javax.persistence.EntityManager
import akka.event.LoggingAdapter
import io.disposia.engine.catalog.mongo.FeedMongoRepository
import io.disposia.engine.catalog.repository.{FeedRepository, RepositoryFactoryBuilder}
import io.disposia.engine.domain.dto.Feed
import io.disposia.engine.mapper.FeedMapper
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

@Deprecated
@Repository
@Transactional
class FeedCatalogService(log: LoggingAdapter,
                         rfb: RepositoryFactoryBuilder)
                        (implicit ec: ExecutionContext)
    extends CatalogService {

    private var repositoryFactory: JpaRepositoryFactory = _
    private var feedRepository: FeedRepository = _

    private val feedMapper = FeedMapper.INSTANCE

    override def refresh(em: EntityManager): Unit = {
        repositoryFactory = rfb.createRepositoryFactory(em)
        feedRepository = repositoryFactory.getRepository(classOf[FeedRepository])
    }

    @Transactional
    def save(feedDTO: Feed): Option[Feed] = {
        log.debug("Request to save Feed : {}", feedDTO)
        Option(feedDTO)
          .map(f => feedMapper.toEntity(f))
          .map(f => feedRepository.save(f))
          .map(f => feedMapper.toImmutable(f))
    }

    @Transactional
    def findOne(dbId: Long): Option[Feed] = {
        log.debug("Request to get Feed (ID) : {}", dbId)
        Option(dbId)
          .map(id => feedRepository.findOne(id))
          .map(f => feedMapper.toImmutable(f))
    }

    @Transactional
    def findOneByExo(feedExo: String): Option[Feed] = {
        log.debug("Request to get Feed (EXO) : {}", feedExo)
        Option(feedExo)
          .map(exo => feedRepository.findOneByExo(exo))
          .map(f => feedMapper.toImmutable(f))
    }

    @Transactional
    def findAll(page: Int, size: Int): List[Feed] = {
        log.debug("Request to get all Feeds by page : {} and size : {}", page, size)
        val pageable = new PageRequest(page, size)
        feedRepository.findAll(pageable)
            .asScala
            .map(f => feedMapper.toImmutable(f))
            .toList
    }

    @Transactional
    def findAllByUrl(url: String): List[Feed] = {
        log.debug("Request to get all Feeds by URL : {}", url)
        feedRepository.findAllByUrl(url)
            .asScala
            .map(f => feedMapper.toImmutable(f))
            .toList
    }

    @Transactional
    def findOneByUrlAndPodcastExo(url: String, podcastExo: String): Option[Feed] = {
        log.debug("Request to get all Feeds by URL : {} and Podcast (EXO) : {}", url, podcastExo)
        val result = feedRepository.findOneByUrlAndPodcastExo(url, podcastExo)
        Option(feedMapper.toImmutable(result))
    }

    @Transactional
    def findAllByPodcast(podcastExo: String): List[Feed] = {
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
