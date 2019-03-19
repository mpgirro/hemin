package hemin.engine.crawler.fetch

import com.typesafe.scalalogging.Logger
import hemin.engine.HeminException
import hemin.engine.crawler.fetch.impl.{FetchImpl, FileFetchImpl, HttpFetchImpl}
import hemin.engine.crawler.fetch.result.{FetchResult, HeadResult}

import scala.concurrent.duration._
import scala.util.{Failure, Try}

object Fetcher {

  def isFeedMime(mime: String): Boolean = mime match {
    case "application/rss+xml"      => true // feed
    case "application/xml"          => true // feed
    case "text/xml"                 => true // feed
    case "text/html"                => true // website
    case "text/plain"               => true // might be ok and might be not -> will have to check manually
    case "none/none"                => true // might be ok and might be not -> will have to check manually
    case "application/octet-stream" => true // some sites use this, but might also be used for media files
    case _                          => false
  }

  def isImageMime(mime: String): Boolean = mime match {
    case "image/jpeg" => true
    case "image/png"  => true
    case _            => false
  }

  def isHtmlMime(mime: String): Boolean = mime match {
    case "text/html" => true
    case _           => false
  }

}

class Fetcher (downloadTimeout: Long, downloadMaxBytes: Long) {

  private val log = Logger(getClass)

  private def impls: List[FetchImpl] = List(
    new HttpFetchImpl(downloadTimeout.seconds, downloadMaxBytes),
    new FileFetchImpl,
  )

  def close(): Unit = {
    log.debug(s"Closing ${this.getClass.getSimpleName}")
    impls.foreach(_.close())
  }

  def check(url: String, isValidMime: String => Boolean): Try[HeadResult] = implForUrl(url)
    .map(_.check(url, isValidMime))
    .getOrElse(failureNoMatchingImpl(url))

  def fetch(url: String, encoding: Option[String], isValidMime: String => Boolean): Try[FetchResult] = implForUrl(url)
    .map(_.fetch(url, encoding, isValidMime))
    .getOrElse(failureNoMatchingImpl(url))

  private def implForUrl(url: String): Option[FetchImpl] = impls.find(_.isSpecificProtocol(url))

  private def failureNoMatchingImpl(url: String): Try[Nothing] =
    Failure(new HeminException(s"No FetchImpl found for URL : '$url'"))

}
