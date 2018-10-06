package util

import java.time.LocalDateTime

import io.disposia.engine.domain.{FeedStatus, _}
import io.disposia.engine.util.mapper.DateMapper
import play.api.libs.json._

import scala.collection.JavaConverters._

object JsonWrites {

  private def toNullB(b: Option[Boolean]): JsValue = b.map(toJson).getOrElse(JsNull)
  private def toNullI(i: Option[Int]): JsValue = i.map(toJson).getOrElse(JsNull)
  private def toNullL(l: Option[Long]): JsValue = l.map(toJson).getOrElse(JsNull)
  private def toNullS(s: Option[String]): JsValue = s.map(toJson).getOrElse(JsNull)
  private def toNullD(d: Option[LocalDateTime]): JsValue = d.map(toJson).getOrElse(JsNull)
  private def toNullF(f: Option[FeedStatus]): JsValue =f.map(toJson).getOrElse(JsNull)

  private def toNullA(opt: Option[Iterable[String]]): JsArray = opt match {
    case Some(as) => jsonFromStringIterable(as)
    case None => JsArray()
  }

  /*
  private def toNullA(opt: Option[Array[String]]): JsArray = opt match {
    case Some(as) => jsonFromStringIterable(as)
    case None => JsArray()
  }

  */

  private def toNullJson(b: Boolean): JsValue = Option(b).map(toJson).getOrElse(JsNull)
  private def toNullJson(i: Int): JsValue = Option(i).map(toJson).getOrElse(JsNull)
  private def toNullJson(l: Long): JsValue = Option(l).map(toJson).getOrElse(JsNull)
  private def toNullJson(s: String): JsValue = Option(s).map(toJson).getOrElse(JsNull)
  private def toNullJson(d: LocalDateTime): JsValue = Option(d).map(toJson).getOrElse(JsNull)
  private def toNullJson(f: FeedStatus): JsValue = Option(f).map(toJson).getOrElse(JsNull)
  private def toNullJson(is: java.util.Set[String]): JsValue = Option(is).map(toJson).getOrElse(JsNull)
  private def toNullJson(is: java.util.List[IndexDoc]): JsValue = Option(is).map(toJson).getOrElse(JsNull)
  private def toNullJson(is: Iterable[IndexDoc]): JsValue = Option(is).map(jsonFromDocumentIterable).getOrElse(JsNull)

  private def toJson(b: Boolean): JsBoolean = JsBoolean(b)
  private def toJson(i: Int): JsNumber = JsNumber(i.toInt)
  private def toJson(l: Long): JsNumber = JsNumber(l)
  private def toJson(s: String): JsString = JsString(s)
  private def toJson(d: LocalDateTime): JsValue = DateMapper.asString(d).map(x => toJson(x)).getOrElse(JsNull)
  private def toJson(f: FeedStatus): JsString = JsString(f.getName)
  private def toJson(ss: java.util.Set[String]): JsArray = jsonFromStringIterable(ss.asScala)
  private def toJson(is: java.util.List[IndexDoc]): JsArray = jsonFromDocumentIterable(is.asScala)

  private def jsonFromStringIterable(ss: Iterable[String]): JsArray = JsArray(ss.map(s => JsString(s)).toVector)
  private def jsonFromDocumentIterable(is: Iterable[IndexDoc]): JsArray = JsArray(is.map(implicitIndexDocWrites.writes).toVector)


  implicit def implicitArrayWrites[T](implicit fmt: Writes[T]): Writes[ArrayWrapper[T]] =
    (as: ArrayWrapper[T]) => JsObject(Seq(
      "results" -> JsArray(as.results.map(fmt.writes).toVector)
    ))

  /**
    * Mapping to write a ResultWrapper out as a JSON value.
    */
  implicit val implicitWrapperWrites: Writes[ResultsWrapper] =
    (w: ResultsWrapper) => JsObject(List(
      "currPage"  -> toNullJson(w.currPage),
      "maxPage"   -> toNullJson(w.maxPage),
      "totalHits" -> toNullJson(w.totalHits),
      "results"   -> toNullJson(w.results)
    ))

  /**
    * Mapping to write a IndexDoc out as a JSON value.
    */
  implicit val implicitIndexDocWrites: Writes[IndexDoc] =
    (d: IndexDoc) => JsObject(List(
      "docType"      -> toNullS(d.docType),
      "id"           -> toNullS(d.id),
      "title"        -> toNullS(d.title),
      "link"         -> toNullS(d.link),
      "pubDate"      -> toNullD(d.pubDate),
      "description"  -> toNullS(d.description),
      "podcastTitle" -> toNullS(d.podcastTitle),
      "image"        -> toNullS(d.image)
    ))

  implicit val implicitPodcastWrites: Writes[Podcast] =
    (p: Podcast) => JsObject(List(
      "id"                    -> toNullS(p.id),
      "title"                 -> toNullS(p.title),
      "link"                  -> toNullS(p.link),
      "pubDate"               -> toNullD(p.pubDate),
      "description"           -> toNullS(p.description),
      "image"                 -> toNullS(p.image),
      "itunesCategories"      -> toNullA(p.itunes.categories),
      "itunesSummary"         -> toNullS(p.itunes.summary),
      "itunesAuthor"          -> toNullS(p.itunes.author),
      "itunesKeywords"        -> toNullA(p.itunes.keywords.map(ks => ks.toIterable)),
      "itunesExplicit"        -> toNullB(p.itunes.explicit),
      "itunesBlock"           -> toNullB(p.itunes.block),
      "itunesType"            -> toNullS(p.itunes.podcastType),
      "language"              -> toNullS(p.meta.language),
      "generator"             -> toNullS(p.meta.generator),
      "copyright"             -> toNullS(p.meta.copyright),
      "registrationTimestamp" -> toNullD(p.registration.timestamp),
      "registrationComplete"  -> toNullB(p.registration.complete)
    ))

  implicit val implicitEpisodeWrites: Writes[Episode] =
    (e: Episode) => JsObject(List(
      "id"                    -> toNullS(e.id),
      "podcastId"             -> toNullS(e.podcastId),
      "podcastTitle"          -> toNullS(e.podcastTitle),
      "title"                 -> toNullS(e.title),
      "link"                  -> toNullS(e.link),
      "pubDate"               -> toNullD(e.pubDate),
      "description"           -> toNullS(e.description),
      "image"                 -> toNullS(e.image),
      "guid"                  -> toNullS(e.guid),
      "guidIsPermalink"       -> toNullB(e.guidIsPermalink),
      "itunesDuration"        -> toNullS(e.itunes.duration),
      "itunesSubtitle"        -> toNullS(e.itunes.subtitle),
      "itunesSeason"          -> toNullI(e.itunes.season),
      "itunesEpisode"         -> toNullI(e.itunes.episode),
      "itunesEpisodeType"     -> toNullS(e.itunes.episodeType),
      "enclosureUrl"          -> toNullS(e.enclosure.url),
      "enclosureLength"       -> toNullL(e.enclosure.length),
      "enclosureType"         -> toNullS(e.enclosure.typ),
      "contentEncoded"        -> toNullS(e.contentEncoded),
      "registrationTimestamp" -> toNullD(e.registration.timestamp)
    ))

  implicit val implicitFeedWrites: Writes[Feed] =
    (f: Feed) => JsObject(List(
      "id"                    -> toNullS(f.id),
      "podcastId"             -> toNullS(f.podcastId),
      "url"                   -> toNullS(f.url),
      "lastChecked"           -> toNullD(f.lastChecked),
      "lastStatus"            -> toNullF(f.lastStatus),
      "registrationTimestamp" -> toNullD(f.registrationTimestamp)
    ))

  implicit val implicitChapterWrites: Writes[Chapter] =
    (c: Chapter) => JsObject(List(
      "start"     -> toNullS(c.start),
      "title"     -> toNullS(c.title),
      "href"      -> toNullS(c.href),
      "image"     -> toNullS(c.image),
      "episodeId" -> toNullS(c.episodeId)
    ))

}
