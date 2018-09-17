package exo.engine.catalog.repository

import exo.engine.domain.entity.PodcastEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.data.repository.query.Param

/**
  * @author Maximilian Irro
  */
trait PodcastRepository extends JpaRepository[PodcastEntity, java.lang.Long] {

    def findOneByExo(exo: String): PodcastEntity

    @Query("SELECT DISTINCT podcast FROM PodcastEntity podcast " +
           "LEFT JOIN podcast.feeds feed " +
           "WHERE feed.exo = :feedExo")
    def findOneByFeed(@Param("feedExo") feedExo: String): PodcastEntity

    def findByRegistrationCompleteTrue(pageable: Pageable): java.util.List[PodcastEntity]

    @Query("SELECT count(podcast) FROM PodcastEntity podcast")
    def countAll(): Long

    @Query("SELECT count(podcast) FROM PodcastEntity podcast WHERE podcast.registrationComplete = true")
    def countAllRegistrationCompleteTrue(): Long
}
