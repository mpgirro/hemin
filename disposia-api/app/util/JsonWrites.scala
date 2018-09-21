package util

import java.time.LocalDateTime

import io.disposia.engine.domain.dto.{IndexDoc, ResultWrapper}
import io.disposia.engine.mapper.DateMapper
import play.api.libs.json._

import scala.collection.JavaConverters._

/**
  * @author max
  */
object JsonWrites {

    private def toNullJson(i: Integer): JsValue = Option(i).map(toJson).getOrElse(JsNull)
    private def toNullJson(s: String): JsValue = Option(s).map(toJson).getOrElse(JsNull)
    private def toNullJson(d: LocalDateTime): JsValue = Option(d).map(toJson).getOrElse(JsNull)
    private def toNullJson(is: java.util.List[IndexDoc]): JsValue = Option(is).map(toJson).getOrElse(JsNull)
    private def toNullJson(is: Iterable[IndexDoc]): JsValue = Option(is).map(toJson).getOrElse(JsNull)

    private def toJson(i: Integer): JsNumber = JsNumber(i.toInt)
    private def toJson(s: String): JsString = JsString(s)
    private def toJson(d: LocalDateTime): JsString = toJson(DateMapper.INSTANCE.asString(d))
    private def toJson(is: java.util.List[IndexDoc]): JsArray = toJson(is.asScala)
    private def toJson(is: Iterable[IndexDoc]): JsArray = JsArray(is.map(implicitIndexDocWrites.writes).toVector)

    /**
      * Mapping to write a ResultWrapper out as a JSON value.
      */
    implicit val implicitWrapperWrites: Writes[ResultWrapper] = (w: ResultWrapper) => JsObject(
        List(
            "currPage"  -> toNullJson(w.getCurrPage),
            "maxPage"   -> toNullJson(w.getMaxPage),
            "totalHits" -> toNullJson(w.getTotalHits),
            "results"   -> toNullJson(w.getResults)
        )
    )

    /**
      * Mapping to write a IndexDoc out as a JSON value.
      */
    implicit val implicitIndexDocWrites: Writes[IndexDoc] = (d: IndexDoc) => JsObject(
        List(
            "docType"      -> toNullJson(d.getDocType),
            "exo"          -> toNullJson(d.getExo),
            "title"        -> toNullJson(d.getTitle),
            "link"         -> toNullJson(d.getLink),
            "pubDate"      -> toNullJson(d.getPubDate),
            "description"  -> toNullJson(d.getDescription),
            "podcastTitle" -> toNullJson(d.getPodcastTitle),
            "image"        -> toNullJson(d.getImage)
        )
    )

}
