package echo.actor.gateway

import java.time.LocalDateTime

import echo.core.domain.dto.{FeedDTO, ImmutableFeedDTO}
import echo.core.domain.feed.FeedStatus
import echo.core.mapper.DateMapper
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNull, JsObject, JsString, JsValue, RootJsonFormat}

/**
  * @author Maximilian Irro
  */
object FeedJsonProtocol extends DefaultJsonProtocol {
    implicit object FeedJsonFormat extends RootJsonFormat[FeedDTO] {
        def write(f: FeedDTO) = JsObject(
            "url"         -> Option(f.getUrl).map(value => JsString(value)).getOrElse(JsNull),
            "lastChecked" -> Option(f.getLastChecked).map(value => JsString(DateMapper.INSTANCE.asString(value))).getOrElse(JsNull),
            "lastStatus"  -> Option(f.getLastStatus).map(value => JsString(value.getName)).getOrElse(JsNull)
        )
        def read(value: JsValue): FeedDTO = {
            value.asJsObject.getFields("url", "lastChecked", "lastStatus") match {
                case Seq(JsString(url), JsString(lastChecked), JsString(lastStatus)) =>

                    ImmutableFeedDTO.builder()
                        .setUrl(url)
                        .setLastChecked(DateMapper.INSTANCE.asLocalDateTime(lastChecked))
                        .setLastStatus(FeedStatus.getByName(lastStatus))
                        .create()

                case _ => throw DeserializationException("FeedDTO expected")
            }
        }
    }
}
