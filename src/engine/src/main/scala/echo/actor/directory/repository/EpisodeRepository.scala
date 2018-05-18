package echo.actor.directory.repository

import echo.core.domain.entity.{EpisodeEntity, PodcastEntity}
import org.springframework.data.jpa.repository.{JpaRepository, Query}
import org.springframework.data.repository.query.Param

/**
  * @author Maximilian Irro
  */
trait EpisodeRepository extends JpaRepository[EpisodeEntity, java.lang.Long] {

    def findOneByExo(exo: String): EpisodeEntity

    def findAllByPodcast(podcast: PodcastEntity): java.util.List[EpisodeEntity]

    @Query("SELECT DISTINCT episode FROM EpisodeEntity episode WHERE episode.podcast.exo = :podcastExo")
    def findAllByPodcastExo(@Param("podcastExo") podcastExo: String): java.util.List[EpisodeEntity]

    @Query("SELECT DISTINCT episode FROM EpisodeEntity episode WHERE episode.podcast.exo = :podcastExo AND episode.guid = :guid")
    def findAllByPodcastAndGuid(@Param("podcastExo") podcastExo: String,
                                @Param("guid") guid: String): java.util.List[EpisodeEntity]

    @Query("SELECT DISTINCT episode FROM EpisodeEntity episode " + "" +
           "WHERE episode.enclosureUrl = :enclosureUrl " +
           "AND episode.enclosureLength = :enclosureLength " +
           "AND episode.enclosureType = :enclosureType")
    def findOneByEnlosure(@Param("enclosureUrl") enclosureUrl: String,
                          @Param("enclosureLength") enclosureLength: Long,
                          @Param("enclosureType") enclosureType: String): EpisodeEntity

    @Query("SELECT count(episode) FROM EpisodeEntity episode")
    def countAll(): Long

    @Query("SELECT count(episode) FROM EpisodeEntity episode WHERE episode.podcast.exo = :podcastExo")
    def countByPodcast(@Param("podcastExo") podcastExo: String): Long
}
