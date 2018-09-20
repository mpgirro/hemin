package v1.search

import java.time.LocalDateTime

import exo.engine.domain.dto.{IndexDoc, ResultWrapper}
import exo.engine.mapper.DateMapper
import play.api.libs.json._
import spray.json.JsField

import scala.collection.JavaConverters._

/**
  * @author max
  */
object ResultWrapperWrites {

    private def toNullableJson(i: Integer): JsValue = Option(i).map(toJson).getOrElse(JsNull)
    private def toNullableJson(s: String): JsValue = Option(s).map(toJson).getOrElse(JsNull)
    private def toNullableJson(d: LocalDateTime): JsValue = Option(d).map(toJson).getOrElse(JsNull)
    private def toNullableJson(is: java.util.List[IndexDoc]): JsValue = Option(is).map(toJson).getOrElse(JsNull)
    private def toNullableJson(is: Iterable[IndexDoc]): JsValue = Option(is).map(toJson).getOrElse(JsNull)

    private def toJson(i: Integer): JsNumber = JsNumber(i.toInt)
    private def toJson(s: String): JsString = JsString(s)
    private def toJson(d: LocalDateTime): JsString = toJson(DateMapper.INSTANCE.asString(d))
    private def toJson(is: java.util.List[IndexDoc]): JsArray = toJson(is.asScala)
    private def toJson(is: Iterable[IndexDoc]): JsArray = JsArray(is.map(implicitIndexDocWrites.writes).toVector)

    /**
      * Mapping to write a ResultWrapper out as a JSON value.
      */
    implicit val implicitWrapperWrites: Writes[ResultWrapper] = (rw: ResultWrapper) => JsObject(
        List(
            "currPage"  -> toNullableJson(rw.getCurrPage),
            "maxPage"   -> toNullableJson(rw.getMaxPage),
            "totalHits" -> toNullableJson(rw.getTotalHits),
            "results"   -> toNullableJson(rw.getResults)
        )
    )

    /**
      * Mapping to write a IndexDoc out as a JSON value.
      */
    implicit val implicitIndexDocWrites: Writes[IndexDoc] = (r: IndexDoc) => JsObject(
        List(
            "docType"      -> toNullableJson(r.getDocType),
            "exo"          -> toNullableJson(r.getExo),
            "title"        -> toNullableJson(r.getTitle),
            "link"         -> toNullableJson(r.getLink),
            "pubDate"      -> toNullableJson(r.getPubDate),
            "description"  -> toNullableJson(r.getDescription),
            "podcastTitle" -> toNullableJson(r.getPodcastTitle),
            "image"        -> toNullableJson(r.getImage)
        )
    )

}
