package api.v1.utils

import io.hemin.engine.model.info._
import io.hemin.engine.model.{FeedStatus, _}
import play.api.libs.json._

object JsonWrites {

  /**
    * Mapping to write a IndexDoc out as a JSON value.
    */
  implicit val indexDocWrites: Writes[IndexDoc] = Json.writes[IndexDoc]

  /**
    * Mapping to write a ResultWrapper out as a JSON value.
    */
  implicit val resultPageWrites: Writes[ResultPage] = Json.writes[ResultPage]

  implicit val atomLinkWrites: Writes[AtomLink] = Json.writes[AtomLink]

  implicit val podcastRegistrationWrites: Writes[PodcastRegistrationInfo] = Json.writes[PodcastRegistrationInfo]

  implicit val podcastItunesWrites: Writes[PodcastItunesInfo] = Json.writes[PodcastItunesInfo]

  implicit val podcastFeedpressWrites: Writes[PodcastFeedpressInfo] = Json.writes[PodcastFeedpressInfo]

  implicit val podcastFyydWrites: Writes[PodcastFyydInfo] = Json.writes[PodcastFyydInfo]

  implicit val podcastWrites: Writes[Podcast] = Json.writes[Podcast]

  implicit val chapterWrites: Writes[Chapter] = Json.writes[Chapter]

  implicit val episodeItunesWrites: Writes[EpisodeItunesInfo] = Json.writes[EpisodeItunesInfo]

  implicit val episodeEnclosureWrites: Writes[EpisodeEnclosureInfo] = Json.writes[EpisodeEnclosureInfo]

  implicit val episodeRegistrationWrites: Writes[EpisodeRegistrationInfo] = Json.writes[EpisodeRegistrationInfo]

  implicit val episodeWrites: Writes[Episode] = Json.writes[Episode]

  implicit val feedStatusWrites: Writes[FeedStatus] = (status: FeedStatus) => JsString(status.entryName)

  implicit val feedWrites: Writes[Feed] = Json.writes[Feed]

  implicit val imageWrites: Writes[Image] = Json.writes[Image]

  implicit def arrayWrites[T](implicit fmt: Writes[T]): Writes[ArrayWrapper[T]] =
    (as: ArrayWrapper[T]) => JsObject(Seq(
      "results" -> JsArray(as.results.map(fmt.writes).toVector)
    ))

}
