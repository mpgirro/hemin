package util

import java.time.LocalDateTime

import io.disposia.engine.domain.dto.{IndexDoc, Podcast, ResultWrapper}
import io.disposia.engine.mapper.DateMapper
import play.api.libs.json._

import scala.collection.JavaConverters._

/**
  * @author max
  */
object JsonWrites {

    private def toNullJson(b: Boolean): JsValue = Option(b).map(toJson).getOrElse(JsNull)
    private def toNullJson(i: Integer): JsValue = Option(i).map(toJson).getOrElse(JsNull)
    private def toNullJson(s: String): JsValue = Option(s).map(toJson).getOrElse(JsNull)
    private def toNullJson(d: LocalDateTime): JsValue = Option(d).map(toJson).getOrElse(JsNull)
    private def toNullJson(is: java.util.Set[String]): JsValue = Option(is).map(toJson).getOrElse(JsNull)
    private def toNullJson(is: java.util.List[IndexDoc]): JsValue = Option(is).map(toJson).getOrElse(JsNull)
    private def toNullJson(is: Iterable[IndexDoc]): JsValue = Option(is).map(jsonFromDocumentIterable).getOrElse(JsNull)

    private def toJson(b: Boolean): JsBoolean = JsBoolean(b)
    private def toJson(i: Integer): JsNumber = JsNumber(i.toInt)
    private def toJson(s: String): JsString = JsString(s)
    private def toJson(d: LocalDateTime): JsString = toJson(DateMapper.INSTANCE.asString(d))
    private def toJson(ss: java.util.Set[String]): JsArray = jsonFromStringIterable(ss.asScala)
    private def toJson(is: java.util.List[IndexDoc]): JsArray = jsonFromDocumentIterable(is.asScala)

    private def jsonFromStringIterable(ss: Iterable[String]): JsArray = JsArray(ss.map(s => JsString(s)).toVector)
    private def jsonFromDocumentIterable(is: Iterable[IndexDoc]): JsArray = JsArray(is.map(implicitIndexDocWrites.writes).toVector)


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

    implicit val implicitPodcastWrites: Writes[Podcast] = (p: Podcast) => JsObject(
        List(
            "exo"                   -> toNullJson(p.getExo),
            "title"                 -> toNullJson(p.getTitle),
            "link"                  -> toNullJson(p.getLink),
            "pubDate"               -> toNullJson(p.getPubDate),
            "description"           -> toNullJson(p.getDescription),
            "image"                 -> toNullJson(p.getImage),
            "itunesCategories"      -> toNullJson(p.getItunesCategories),
            "itunesSummary"         -> toNullJson(p.getItunesSummary),
            "itunesAuthor"          -> toNullJson(p.getItunesAuthor),
            "itunesKeywords"        -> toNullJson(p.getItunesKeywords),
            "itunesExplicit"        -> toNullJson(p.getItunesExplicit),
            "itunesBlock"           -> toNullJson(p.getItunesBlock),
            "itunesType"            -> toNullJson(p.getItunesType),
            "language"              -> toNullJson(p.getLanguage),
            "generator"             -> toNullJson(p.getGenerator),
            "copyright"             -> toNullJson(p.getGenerator),
            "episodeCount"          -> toNullJson(p.getEpisodeCount),
            "registrationTimestamp" -> toNullJson(p.getRegistrationTimestamp),
            "registrationComplete"  -> toNullJson(p.getRegistrationComplete),

        )
    )

}
