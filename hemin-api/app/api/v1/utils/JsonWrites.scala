package api.v1.utils

import io.hemin.engine.model._
import play.api.libs.json._

object JsonWrites {

  /** Mapping to write a IndexDoc out as a JSON value. */
  implicit val indexDocWrites: Writes[IndexDoc] = Json.writes[IndexDoc]

  /** Mapping to write a ResultWrapper out as a JSON value. */
  implicit val searchResultWrites: Writes[SearchResult] = Json.writes[SearchResult]

  implicit val atomLinkWrites: Writes[AtomLink] = Json.writes[AtomLink]

  implicit val podcastRegistrationWrites: Writes[PodcastRegistration] = Json.writes[PodcastRegistration]

  implicit val podcastItunesWrites: Writes[PodcastItunes] = Json.writes[PodcastItunes]

  implicit val podcastFeedpressWrites: Writes[PodcastFeedpress] = Json.writes[PodcastFeedpress]

  implicit val podcastFyydWrites: Writes[PodcastFyyd] = Json.writes[PodcastFyyd]

  implicit val podcastWrites: Writes[Podcast] = Json.writes[Podcast]

  implicit val chapterWrites: Writes[Chapter] = Json.writes[Chapter]

  implicit val episodeItunesWrites: Writes[EpisodeItunes] = Json.writes[EpisodeItunes]

  implicit val episodeEnclosureWrites: Writes[EpisodeEnclosure] = Json.writes[EpisodeEnclosure]

  implicit val episodeRegistrationWrites: Writes[EpisodeRegistration] = Json.writes[EpisodeRegistration]

  implicit val episodeWrites: Writes[Episode] = Json.writes[Episode]

  implicit val feedStatusWrites: Writes[FeedStatus] = (status: FeedStatus) => JsString(status.entryName)

  implicit val feedWrites: Writes[Feed] = Json.writes[Feed]

  implicit val imageWrites: Writes[Image] = Json.writes[Image]

  implicit def arrayWrites[T](implicit fmt: Writes[T]): Writes[ArrayWrapper[T]] =
    (as: ArrayWrapper[T]) => JsObject(Seq(
      "results" -> JsArray(as.results.map(fmt.writes).toVector)
    ))

}
