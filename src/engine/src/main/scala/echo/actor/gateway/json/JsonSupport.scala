package echo.actor.gateway.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import echo.actor.gateway.FeedJsonProtocol
import spray.json.{DefaultJsonProtocol, JsonFormat}

/**
  * @author Maximilian Irro
  */

// Required to protect against JSON Hijacking for Older Browsers: Always return JSON with an Object on the outside
case class ArrayWrapper[T](results: T)

// collect your json format instances into a support trait:
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit def arrayWrapper[T: JsonFormat] = jsonFormat1(ArrayWrapper.apply[T]) // TODO unsued?
    implicit val resultFormat = IndexResultJsonProtocol.IndexResultJsonFormat
    implicit val podcastFormat = PodcastJsonProtocol.PodcastJsonFormat
    implicit val episodeFormat = EpisodeJsonProtocol.EpisodeJsonFormat
    implicit val feedFormat = FeedJsonProtocol.FeedJsonFormat
    implicit val chapterFormat = ChapterJsonProtocol.ChapterJsonFormat
    implicit val resultWrapperFormat = ResultWrapperJsonProtocol.ResultWrapperJsonFormat
}
