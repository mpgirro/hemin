package exo.engine.catalog

import java.time.LocalDateTime

import exo.engine.domain.FeedStatus
import exo.engine.domain.dto.{ChapterDTO, EpisodeDTO, FeedDTO, PodcastDTO}

/**
  * @author Maximilian Irro
  */
object CatalogProtocol {

    trait CatalogCommand

    case class ProposeNewFeed(url: String) extends CatalogCommand                 // Web/CLI -> CatalogStore
    case class RegisterEpisodeIfNew(podcastExo: String, episode: EpisodeDTO) extends CatalogCommand // Questions: Parser -> CatalogStore


    trait CatalogEvent

    case class AddPodcastAndFeedIfUnknown(podcast: PodcastDTO, feed: FeedDTO) extends CatalogEvent

    // Crawler -> CatalogStore
    case class FeedStatusUpdate(podcastExo: String, feedUrl: String, timestamp: LocalDateTime, status: FeedStatus) extends CatalogEvent
    case class UpdateFeedUrl(oldUrl: String, newUrl: String) extends CatalogEvent
    case class UpdateLinkByExo(exo: String, newUrl: String) extends CatalogEvent

    case class SaveChapter(chapter: ChapterDTO) extends CatalogEvent

    // Parser -> CatalogStore
    case class UpdatePodcast(podcastExo: String, feedUrl: String, podcast: PodcastDTO) extends CatalogEvent
    case class UpdateEpisode(podcastExo: String, episode: EpisodeDTO) extends CatalogEvent
    case class UpdateEpisodeWithChapters(podcastExo: String, episode: EpisodeDTO, chapter: List[ChapterDTO]) extends CatalogEvent


    trait CatalogQuery

    // Gateway -> CatalogStore
    case class GetPodcast(podcastExo: String) extends CatalogQuery
    case class GetAllPodcasts(page: Int, size: Int) extends CatalogQuery
    case class GetAllPodcastsRegistrationComplete(page: Int, size: Int) extends CatalogQuery
    case class GetAllFeeds(page: Int, size: Int) extends CatalogQuery
    case class GetEpisode(episodeExo: String) extends CatalogQuery
    case class GetEpisodesByPodcast(podcastExo: String) extends CatalogQuery
    case class GetFeedsByPodcast(podcastExo: String) extends CatalogQuery
    case class GetChaptersByEpisode(episodeExo: String) extends CatalogQuery

    // Web/CLI -> CatalogStore
    case class CheckPodcast(exo: String) extends CatalogQuery
    case class CheckFeed(exo: String) extends CatalogQuery
    case class CheckAllPodcasts() extends CatalogQuery
    case class CheckAllFeeds() extends CatalogQuery


    trait CatalogQueryResult

    // CatalogStore -> Gateway
    case class PodcastResult(podcast: PodcastDTO) extends CatalogQueryResult
    case class AllPodcastsResult(results: List[PodcastDTO]) extends CatalogQueryResult
    case class AllFeedsResult(results: List[FeedDTO]) extends CatalogQueryResult
    case class EpisodeResult(episode: EpisodeDTO) extends CatalogQueryResult
    case class EpisodesByPodcastResult(episodes: List[EpisodeDTO]) extends CatalogQueryResult
    case class FeedsByPodcastResult(feeds: List[FeedDTO]) extends CatalogQueryResult
    case class ChaptersByEpisodeResult(chapters: List[ChapterDTO]) extends CatalogQueryResult
    case class NothingFound(exo: String) extends CatalogQueryResult

    // some stuff for statistics
    case class GetMeanEpisodeCountPerPodcast()
    case class MeanEpisodeCountPerPodcast(podcastCount: Int, episodeCount: Int, mean: Int)

}
