package exo.engine.catalog.service

import javax.persistence.EntityManager
import akka.event.LoggingAdapter
import exo.engine.catalog.repository.{ChapterRepository, RepositoryFactoryBuilder}
import exo.engine.domain.dto.{Chapter, ModifiableChapter}
import exo.engine.mapper.ChapterMapper
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
class ChapterCatalogService(log: LoggingAdapter,
                            rfb: RepositoryFactoryBuilder) extends CatalogService {

    private var repositoryFactory: JpaRepositoryFactory = _
    private var chapterRepository: ChapterRepository = _

    private val chapterMapper = ChapterMapper.INSTANCE

    override def refresh(em: EntityManager): Unit = {
        repositoryFactory = rfb.createRepositoryFactory(em)
        chapterRepository = repositoryFactory.getRepository(classOf[ChapterRepository])
    }

    @Transactional
    def save(chapterDTO: Chapter): Option[Chapter] = {
        log.debug("Request to save Chapter : {}", chapterDTO)
        Option(chapterDTO)
          .map(c => chapterMapper.toEntity(c))
          .map(c => chapterRepository.save(c))
          .map(c => chapterMapper.toModifiable(c))
          .map(_.toImmutable)
    }

    @Transactional
    def saveAll(episodeId: Long, chapters: java.util.List[Chapter]): Unit = {
        log.debug("Request to save Chapters for Episode (ID) : {}", episodeId)
        for(capter <- chapters.asScala){
            val c = new ModifiableChapter().from(capter)
            c.setEpisodeId(episodeId)
            save(c)
        }
    }

    @Transactional
    def findAllByEpisode(episodeExo: String): List[Chapter] = {
        log.debug("Request to get all Chapters by Episode (EXO) : {}", episodeExo)
        chapterRepository.findAllByEpisodeExo(episodeExo)
            .asScala
            .map(c => chapterMapper.toModifiable(c).toImmutable)
            .toList
    }

}
