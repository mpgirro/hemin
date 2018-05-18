package echo.actor.directory

import java.time.LocalDateTime

import echo.core.domain.dto.{ChapterDTO, EpisodeDTO, FeedDTO, PodcastDTO}
import echo.core.domain.feed.FeedStatus

/**
  * @author Maximilian Irro
  */
object DirectoryProtocol {

    trait DirectoryCommand

    case class ProposeNewFeed(url: String) extends DirectoryCommand                                   // Web/CLI -> DirectoryStore
    case class RegisterEpisodeIfNew(podcastExo: String, episode: EpisodeDTO) extends DirectoryCommand // Questions: Parser -> DirectoryStore


    trait DirectoryEvent

    case class AddPodcastAndFeedIfUnknown(podcast: PodcastDTO, feed: FeedDTO) extends DirectoryEvent

    // Crawler -> DirectoryStore
    case class FeedStatusUpdate(podcastExo: String, feedUrl: String, timestamp: LocalDateTime, status: FeedStatus) extends DirectoryEvent
    case class UpdateFeedUrl(oldUrl: String, newUrl: String) extends DirectoryEvent
    case class UpdateLinkByExo(exo: String, newUrl: String) extends DirectoryEvent

    case class SaveChapter(chapter: ChapterDTO) extends DirectoryEvent

    // Parser -> DirectoryStore
    case class UpdatePodcast(podcastExo: String, feedUrl: String, podcast: PodcastDTO) extends DirectoryEvent
    case class UpdateEpisode(podcastExo: String, episode: EpisodeDTO) extends DirectoryEvent
    case class UpdateEpisodeWithChapters(podcastExo: String, episode: EpisodeDTO, chapter: List[ChapterDTO]) extends DirectoryEvent


    trait DirectoryQuery

    // Gateway -> DirectoryStore
    case class GetPodcast(podcastExo: String) extends DirectoryQuery
    case class GetAllPodcasts(page: Int, size: Int) extends DirectoryQuery
    case class GetAllPodcastsRegistrationComplete(page: Int, size: Int) extends DirectoryQuery
    case class GetAllFeeds(page: Int, size: Int) extends DirectoryQuery
    case class GetEpisode(episodeExo: String) extends DirectoryQuery
    case class GetEpisodesByPodcast(podcastExo: String) extends DirectoryQuery
    case class GetFeedsByPodcast(podcastExo: String) extends DirectoryQuery
    case class GetChaptersByEpisode(episodeExo: String) extends DirectoryQuery

    // Web/CLI -> DirectoryStore
    case class CheckPodcast(exo: String) extends DirectoryQuery
    case class CheckFeed(exo: String) extends DirectoryQuery
    case class CheckAllPodcasts() extends DirectoryQuery
    case class CheckAllFeeds() extends DirectoryQuery


    trait DirectoryQueryResult

    // DirectoryStore -> Gateway
    case class PodcastResult(podcast: PodcastDTO) extends DirectoryQueryResult
    case class AllPodcastsResult(results: List[PodcastDTO]) extends DirectoryQueryResult
    case class AllFeedsResult(results: List[FeedDTO]) extends DirectoryQueryResult
    case class EpisodeResult(episode: EpisodeDTO) extends DirectoryQueryResult
    case class EpisodesByPodcastResult(episodes: List[EpisodeDTO]) extends DirectoryQueryResult
    case class FeedsByPodcastResult(feeds: List[FeedDTO]) extends DirectoryQueryResult
    case class ChaptersByEpisodeResult(chapters: List[ChapterDTO]) extends DirectoryQueryResult
    case class NothingFound(exo: String) extends DirectoryQueryResult

}
