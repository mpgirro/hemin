package io.hemin.engine.crawler.http

import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}

import com.softwaremill.sttp.{Response, _}
import com.typesafe.scalalogging.Logger
import io.hemin.engine.EngineException

import scala.concurrent.duration._
import scala.io.Source
import scala.util.{Failure, Success, Try}

class HttpClient (timeout: Long, private val downloadMaxBytes: Long) {

  import io.hemin.engine.crawler.http.HttpClient._ // import the failures

  private val log = Logger(getClass)

  private val downloadTimeout = timeout.seconds

  private val defaultCharset: String = StandardCharsets.UTF_8.name()

  private implicit val sttpBackend: SttpBackend[Id, Nothing] =
    HttpURLConnectionBackend(options = SttpBackendOptions.connectionTimeout(downloadTimeout))

  def close(): Unit = {
    sttpBackend.close()
  }

  def headCheck(url: String, isValidMime: String => Boolean): Try[HeadResult] = Option(url)
    .map(_.split("://"))
    .map(Success(_))
    .getOrElse(headFailureUrlNull)
    .flatMap { parts =>
      if (parts.length != 2) {
        headFailureInvalidUrl(url)
      } else {
        Success(parts(0).toLowerCase + "://" + parts(1))
      }
    }
    .flatMap { ref =>
      if (ref.startsWith("http://") || ref.startsWith("https://")) {
        headCheckHTTP(ref, isValidMime)
      } else if (ref.startsWith("file:///")) {
        headCheckFILE(ref, isValidMime)
      } else {
        headFailureInvalidLocation(url)
      }
    }

  def fetchContent(url: String, encoding: Option[String], isValidMime: String => Boolean): Try[(Array[Byte],Option[String],String)] =
    if (url.startsWith("http://") || url.startsWith("https://")) {
      fetchContentHTTP(url, encoding, isValidMime)
    } else if (url.startsWith("file:///")) {
      fetchContentFILE(url, encoding, isValidMime)
    } else {
      fetchFailureInvalidLocation(url)
    }

  private def sendHeadRequest(url: String): Try[Response[String]] = Try {
    val response = emptyRequest // use empty request, because standard req uses header "Accept-Encoding: gzip" which can cause problems with HEAD requests
      .head(uri"$url")
      .readTimeout(downloadTimeout)
      .acceptEncoding("")
      .send()
    response // Note: because the result type of .send() is weird, be need to actually assign the result to a reference
  }

  private def sendGetRequest(url: String, encoding: Option[String]): Try[Response[Array[Byte]]] = Try {
    val request = sttp
      .get(uri"$url")
      .readTimeout(downloadTimeout)
      .response(asByteArray) // prevent assuming UTF-8 encoding, because some feeds do not use it

    encoding.foreach(e => request.acceptEncoding(e))

    val response = request.send()
    response // Note: because the result type of .send() is weird, be need to actually assign the result to a reference
  }

  private def mimeType[T](response: Response[T]): Option[String] = mimeType(response.contentType)

  //private def mimeType(response: Response[Array[Byte]]): Option[String] = mimeType(response.contentType)

  private def mimeType(contentType: Option[String]): Option[String] = contentType
    .map(_.split(";"))
    .map(_(0))    // the first element of the array is the mimeType
    .map(_.trim)  // we've experienced too much whitespace in these strings

  private def contentType(response: Response[String]): Option[String] =
    response.contentType
      .flatMap(_
        .split(";")
        .lift(1)
        .flatMap(_
          .split("=")
          .lift(1) // -> "UTF-8"
          .map(_
          .replaceAll("\"", "") // remove quotation marks if any
          .trim)))

  private def headCheckHTTP(url: String, isValidMime: String => Boolean): Try[HeadResult] =
    sendHeadRequest(url)
      .flatMap { response =>
        if (!response.isSuccess) {
          val code = response.code
          val text = response.statusText
          response.code match {
            case 200 => Success(response) // all fine
            case 302 => Success(response) // odd, but ok
            case 404 => Success(response) // not found: nothing there worth processing
              headFailureInvalidStatus(code, text)
            case 503 => // service unavailable
              headFailureInvalidStatus(code, text)
            case _   =>
              log.warn("Received unexpected status from HEAD request : '{} {}' on '{}'", code, text, url)
              Success(response)
          }
        } else {
          Success(response)
        }
      }
      .map { response =>
        HeadResult(
          statusCode      = response.code,
          location        = response.header("location"),
          mimeType        = mimeType(response),
          contentEncoding = contentType(response),
          eTag            = response.header("etag"),
          lastModified    = response.header("last-modified"),
        )
      }
      .map { head =>
        head.statusCode match {
          case 301 => // Moved Permanently
            log.debug("Redirecting {} to {}", url, head.location.getOrElse("NON PROVIDED"))
            head
          case _ =>
            // Note: We set the URL back to our original information, because that what we know from the DB
            // TODO: is this the smart thing to do?
            head.copy(location = Option(url).orElse(head.location))
        }
      }
      .flatMap { head =>
        head.mimeType match {
          case Some(mime) =>
            if (!isValidMime(mime)) {
              headFailureUnexpectedMime(mime, url)
            } else {
              Success(head)
            }
          case None =>
            // got no content type from HEAD request, therefore I'll just have to download the whole thing and look for myself
            log.warn("Did not get a Content-Type from HEAD request : {}", url)
            Success(head)
        }
      }

  // this method is a remnant from the past and has become obsolete

  private def headCheckFILE(url: String, isValidMime: String => Boolean): Try[HeadResult] = Try {
    val path: Path = Paths.get(url)
    val mimeType: String = java.nio.file.Files.probeContentType(path)
    if (isValidMime(mimeType)) {
      val file = path.toFile
      val status = if (file.exists()) 200 else 404
      HeadResult(
        statusCode      = status,
        location        = Option(url),
        mimeType        = Option(mimeType).orElse(Some("text/xml")),
        contentEncoding = Option("UTF-8"),
        eTag            = None,
        lastModified    = None,
      )
    } else {
      throw new EngineException(s"check failed on MIME : '$mimeType'")
    }
  }

  /**
    *
    * @param url
    * @param encoding
    * @param isValidMime
    * @return Tuple with 1) data (as array of bytes) and 2) encoding if the bytes respresent a string
    */
  private def fetchContentHTTP(url: String, encoding: Option[String], isValidMime: String => Boolean): Try[(Array[Byte],Option[String],String)] =
    sendGetRequest(url, encoding)
      .flatMap { response =>
        if (response.isSuccess) {
          Success(response)
        } else {
          fetchFailureNonSuccessResponse(response.code)
        }
      }
      .flatMap { response =>
        response.contentLength
          .filter(_ > downloadMaxBytes)
          .map(fetchFailureExceedingLength)
          .getOrElse(Success(response))
      }
      .flatMap { response =>
        mimeType(response)
          .filter(!isValidMime(_))
          .map(fetchFailureInvalidMime)
          .getOrElse(Success(response))
      }
      .flatMap { response =>
        response.body match {
          case Left(error) => fetchFailureWithError(error)
          case Right(data) =>
            val mime = mimeType(response)
            val enc = encoding.getOrElse(defaultCharset)
            Success((data, mime, enc))
        }
      }

  //private def mimeType(contentType: Option[String]): Option[String] = contentType.map(_.split(";")(0).trim)

  private def fetchContentFILE(url: String, encoding: Option[String], isValidMime: String => Boolean): Try[(Array[Byte],Option[String],String)] = Try {
    import java.nio.file.{Files, Paths}

    val mime: Option[String] = Try {
      Some(Files.probeContentType(Paths.get(url)))
    }.getOrElse(None)

    val enc: String = encoding.getOrElse(defaultCharset)
    val data = Source
      .fromURL(url)
      .getLines
      .mkString
      .getBytes(enc)
    (data, mime, enc)
  }


  // http, https, ftp if provided
  private def protocol(url: String): String =
    if (url.indexOf("://") > -1) {
      url.split("://")(0)
    } else {
      ""
    }

  private def hostname(url: String): String = {
    if (url.indexOf("://") > -1)
      url.split('/')(2)
    else
      url.split('/')(0)
  }
    .split(':')(0) // find & remove port number
    .split('?')(0) // find & remove "?"
  // .split('/')(0) // find & remove the "/" that might still stick at the end of the hostname

}

object HttpClient {

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

  private def headFailureUrlNull: Try[Nothing] =
    Failure(new EngineException("Cannot run HEAD check; reason: URL was NULL"))

  private def headFailureInvalidUrl(url: String): Try[Nothing] =
    Failure(new EngineException(s"Cannot run HEAD check; reason: no valid URL provided : '$url'"))

  private def headFailureInvalidLocation(url: String): Try[Nothing] =
    Failure(new EngineException(s"Cannot run HEAD check; reason: URL points neither to local nor remote resource : '$url'"))

  private def headFailureInvalidStatus(code: Int, text: String): Try[Nothing] =
    Failure(new EngineException(s"HEAD check reported status $code : '$text'"))

  private def headFailureUnexpectedMime(mime: String, url: String): Try[Nothing] =
    Failure(new EngineException(s"HEAD check received unexpected MIME-type '$mime' of '$url'"))

  private def fetchFailureInvalidLocation(url: String): Try[Nothing] =
    Failure(new EngineException(s"Cannot GET content; reason: URL points neither to local nor remote resource : '$url'"))

  private def fetchFailureWithError(error: String): Try[Nothing] =
    Failure(new EngineException(s"Error collecting download body; message: $error"))

  private def fetchFailureNonSuccessResponse(code: Int): Try[Nothing] =
    Failure(new EngineException(s"Download resulted in a non-success response code : $code"))

  private def fetchFailureInvalidMime(mime: String): Try[Nothing] =
    Failure(new EngineException(s"Aborted before downloading a file with invalid MIME-type : '$mime'"))

  private def fetchFailureExceedingLength(length: Long): Try[Nothing] =
    Failure(new EngineException(s"Refusing to download resource because content length is too much: $length"))
}
