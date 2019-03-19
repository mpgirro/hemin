package hemin.engine.crawler.fetch.impl

import java.nio.charset.StandardCharsets

import hemin.engine.HeminException
import hemin.engine.crawler.fetch.result.{FetchResult, HeadResult}

import scala.util.{Failure, Success, Try}

trait FetchImpl {

  protected[this] val defaultCharset: String = StandardCharsets.UTF_8.name()

  protected[this] def specificCheck(url: String, isValidMime: String => Boolean): Try[HeadResult]

  protected[this] def specificFetch(url: String, encoding: Option[String], isValidMime: String => Boolean): Try[FetchResult]

  def isSpecificProtocol(url: String): Boolean

  def close(): Unit

  def check(url: String, isValidMime: String => Boolean): Try[HeadResult] = Option(url)
    .map(_.split("://"))
    .map(Success(_))
    .getOrElse(Failure(new HeminException("Cannot run HEAD check; reason: URL was NULL")))
    .flatMap { parts =>
      if (parts.length != 2) {
        Failure(new HeminException(s"Cannot run HEAD check; reason: no valid URL provided : '$url'"))
      } else {
        Success(s"${parts(0).toLowerCase}://${parts(1)}")
      }
    }
    .flatMap{ ref =>
      if (isSpecificProtocol(ref)) {
        specificCheck(url, isValidMime)
      } else {
        headFailureInvalidLocation(ref)
      }
    }

  def fetch(url: String, encoding: Option[String], isValidMime: String => Boolean): Try[FetchResult] =
    if (isSpecificProtocol(url)) {
      specificFetch(url, encoding, isValidMime)
    } else {
      headFailureInvalidLocation(url)
    }

  protected[this] def headFailureInvalidLocation(url: String): Try[Nothing] =
    Failure(new HeminException(s"Cannot run HEAD check; reason: URL points neither to local nor remote resource : '$url'"))

  protected[this] def headFailureInvalidStatus(code: Int, text: String): Try[Nothing] =
    Failure(new HeminException(s"HEAD check reported status $code : '$text'"))

  protected[this] def headFailureUnexpectedMime(mime: String, url: String): Try[Nothing] =
    Failure(new HeminException(s"HEAD check received unexpected MIME-type '$mime' of '$url'"))

  protected[this] def fetchFailureWithError(error: String): Try[Nothing] =
    Failure(new HeminException(s"Error collecting download body; message: $error"))

  protected[this] def fetchFailureNonSuccessResponse(code: Int): Try[Nothing] =
    Failure(new HeminException(s"Download resulted in a non-success response code : $code"))

  protected[this] def fetchFailureInvalidMime(mime: String): Try[Nothing] =
    Failure(new HeminException(s"Aborted before downloading a file with invalid MIME-type : '$mime'"))

  protected[this] def fetchFailureExceedingLength(length: Long): Try[Nothing] =
    Failure(new HeminException(s"Refusing to download resource because content length is too much: $length"))

}
