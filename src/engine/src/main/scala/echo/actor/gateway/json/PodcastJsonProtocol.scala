package echo.actor.gateway.json

import java.time.LocalDateTime
import java.util

import echo.core.domain.dto.{ImmutablePodcastDTO, PodcastDTO}
import echo.core.mapper.DateMapper
import spray.json.{DefaultJsonProtocol, DeserializationException, JsArray, JsBoolean, JsNull, JsObject, JsString, JsValue, RootJsonFormat}

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */
object PodcastJsonProtocol extends DefaultJsonProtocol {
    implicit object PodcastJsonFormat extends RootJsonFormat[PodcastDTO] {
        def write(p: PodcastDTO) = JsObject(
            "exo"                   -> Option(p.getExo).map(value => JsString(value)).getOrElse(JsNull),
            "title"                 -> Option(p.getTitle).map(value => JsString(value)).getOrElse(JsNull),
            "link"                  -> Option(p.getLink).map(value => JsString(value)).getOrElse(JsNull),
            "pubDate"               -> Option(p.getPubDate).map(value => JsString(DateMapper.INSTANCE.asString(value))).getOrElse(JsNull),
            "description"           -> Option(p.getDescription).map(value => JsString(value)).getOrElse(JsNull),
            "image"                 -> Option(p.getImage).map(value => JsString(value)).getOrElse(JsNull),
            "itunesCategories"      -> Option(p.getItunesCategories).map(value => JsArray(value.asScala.map(c => JsString(c)).toVector)).getOrElse(JsNull),
            "itunesSummary"         -> Option(p.getItunesSummary).map(value => JsString(value)).getOrElse(JsNull),
            "itunesAuthor"          -> Option(p.getItunesAuthor).map(value => JsString(value)).getOrElse(JsNull),
            "itunesKeywords"        -> Option(p.getItunesKeywords).map(value => JsString(value)).getOrElse(JsNull),
            "itunesExplicit"        -> Option(p.getItunesExplicit).map(value => JsBoolean(value)).getOrElse(JsNull),
            "itunesBlock"           -> Option(p.getItunesBlock).map(value => JsBoolean(value)).getOrElse(JsNull),
            "itunesType"            -> Option(p.getItunesType).map(value => JsString(value)).getOrElse(JsNull),
            "language"              -> Option(p.getLanguage).map(value => JsString(value)).getOrElse(JsNull),
            "generator"             -> Option(p.getGenerator).map(value => JsString(value)).getOrElse(JsNull),
            "copyright"             -> Option(p.getCopyright).map(value => JsString(value)).getOrElse(JsNull),
            "episodeCount"          -> Option(p.getEpisodeCount).map(value => JsString(value.toString)).getOrElse(JsNull),
            "registrationTimestamp" -> Option(p.getRegistrationTimestamp).map(value => JsString(DateMapper.INSTANCE.asString(value))).getOrElse(JsNull),
            "registrationComplete"  -> Option(p.getRegistrationComplete).map(value => JsBoolean(value)).getOrElse(JsNull)
        )
        def read(value: JsValue): PodcastDTO = {
            value.asJsObject.getFields(
                "exo", "title", "link",
                "pubDate", "description",  "image",
                "itunesCategories", "itunesSummary", "itunesAuthor", "itunesKeywords") match {
                case Seq(
                    JsString(exo), JsString(title), JsString(link),
                    JsString(pubDate), JsString(description), JsString(image),
                    JsArray(itunesCategories), JsString(itunesSummary), JsString(itunesAuthor), JsString(itunesKeywords)) =>

                    ImmutablePodcastDTO.builder()
                        .setExo(exo)
                        .setTitle(title)
                        .setLink(link)
                        .setPubDate(DateMapper.INSTANCE.asLocalDateTime(pubDate))
                        .setDescription(description)
                        .setImage(image)
                        .setItunesCategories(new util.HashSet[String](itunesCategories.map(_.convertTo[String]).asJava))
                        .setItunesSummary(itunesSummary)
                        .setItunesAuthor(itunesAuthor)
                        .setItunesKeywords(itunesKeywords)
                        .create()

                case _ => throw DeserializationException("PodcastDTO expected")
            }
        }
    }
}
