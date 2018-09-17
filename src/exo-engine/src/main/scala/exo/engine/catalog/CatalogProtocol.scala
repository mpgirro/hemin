package exo.engine.catalog

import java.time.LocalDateTime

import exo.engine.domain.FeedStatus
import exo.engine.domain.dto._

/**
  * @author Maximilian Irro
  */
object CatalogProtocol {

    trait CatalogMessage

    trait CatalogCommand extends CatalogMessage

    case class ProposeNewFeed(url: String) extends CatalogCommand                 // Web/CLI -> CatalogStore
    case class RegisterEpisodeIfNew(podcastExo: String, episode: Episode) extends CatalogCommand // Questions: Parser -> CatalogStore


    trait CatalogEvent extends CatalogMessage

    case class AddPodcastAndFeedIfUnknown(podcast: Podcast, feed: Feed) extends CatalogEvent

    // Crawler -> CatalogStore
    case class FeedStatusUpdate(podcastExo: String, feedUrl: String, timestamp: LocalDateTime, status: FeedStatus) extends CatalogEvent
    case class UpdateFeedUrl(oldUrl: String, newUrl: String) extends CatalogEvent
    case class UpdateLinkByExo(exo: String, newUrl: String) extends CatalogEvent

    case class SaveChapter(chapter: Chapter) extends CatalogEvent

    // Parser -> CatalogStore
    case class UpdatePodcast(podcastExo: String, feedUrl: String, podcast: Podcast) extends CatalogEvent
    case class UpdateEpisode(podcastExo: String, episode: Episode) extends CatalogEvent
    case class UpdateEpisodeWithChapters(podcastExo: String, episode: Episode, chapter: List[Chapter]) extends CatalogEvent


    trait CatalogQuery extends CatalogMessage

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


    trait CatalogQueryResult extends CatalogMessage

    // CatalogStore -> Gateway
    case class PodcastResult(podcast: Podcast) extends CatalogQueryResult
    case class AllPodcastsResult(results: List[Podcast]) extends CatalogQueryResult
    case class AllFeedsResult(results: List[Feed]) extends CatalogQueryResult
    case class EpisodeResult(episode: Episode) extends CatalogQueryResult
    case class EpisodesByPodcastResult(episodes: List[Episode]) extends CatalogQueryResult
    case class FeedsByPodcastResult(feeds: List[Feed]) extends CatalogQueryResult
    case class ChaptersByEpisodeResult(chapters: List[Chapter]) extends CatalogQueryResult
    case class NothingFound(exo: String) extends CatalogQueryResult

    // some stuff for statistics
    case class GetMeanEpisodeCountPerPodcast()
    case class MeanEpisodeCountPerPodcast(podcastCount: Int, episodeCount: Int, mean: Int)

}
