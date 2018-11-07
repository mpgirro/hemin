package io.hemin.engine.crawler.http

import java.nio.file.Paths

import com.softwaremill.sttp.{Response, _}
import com.typesafe.scalalogging.Logger
import io.hemin.engine.EngineException

import scala.concurrent.duration._
import scala.io.Source
import scala.util.{Failure, Success, Try}

class HttpClient (timeout: Long, private val downloadMaxBytes: Long) {

  private val log = Logger(getClass)

  private val downloadTimeout = timeout.seconds

  private implicit val sttpBackend = HttpURLConnectionBackend(options = SttpBackendOptions.connectionTimeout(downloadTimeout))

  def close(): Unit = {
    sttpBackend.close()
  }

  def headCheck(url: String): Try[HeadResult] = Option(url)
    .map(_.split("://"))
    .map(Success(_))
    .getOrElse(Failure(new EngineException("Cannot run HEAD check; reason : URL was NULL")))
    .flatMap { parts =>
      if (parts.length != 2) {
        Failure(new EngineException("Cannot run HEAD check; reason : no valid URL provided : " + url))
      } else {
        Success(parts(0).toLowerCase + "://" + parts(1))
      }
    }
    .flatMap { ref =>
      if (ref.startsWith("http://") || ref.startsWith("https://")) {
        headCheckHTTP(ref)
      } else if (ref.startsWith("file:///")) {
        headCheckFILE(ref)
      } else {
        Failure(new EngineException("Cannot run HEAD check; reason : URL points neither to local nor remote resource : " + url))
      }
    }

  private def sendHeadRequest(url: String): Try[Response[String]] = Try {
    val response = emptyRequest // use empty request, because standard req uses header "Accept-Encoding: gzip" which can cause problems with HEAD requests
      .head(uri"$url")
      .readTimeout(downloadTimeout)
      .acceptEncoding("")
      .send()
    response // Note: because the result type of .send() is weird, be need to actually assign the result to a reference
  }

  private def mimeType(response: Response[String]): Option[String] =
    response.contentType
      .map(_.split(";"))
      .map(_(0))    // the first element of the array is the mimeType
      .map(_.trim)  // we've experienced to much whitespace in the strings

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

  private def headCheckHTTP(url: String): Try[HeadResult] = sendHeadRequest(url)
    .flatMap { response =>
      if (!response.isSuccess) {
        val code = response.code
        val text = response.statusText
        response.code match {
          case 200 => Success(response) // all fine
          case 302 => Success(response) // odd, but ok
          case 404 => Success(response) // not found: nothing there worth processing
            Failure(new EngineException(s"HEAD request reported status $code : '$text'"))
          case 503 => // service unavailable
            Failure(new EngineException(s"HEAD request reported status $code : '$text'"))
          case _   =>
            log.warn("Received unexpected status from HEAD request : {} {} on {}", code, text, url)
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
            mime match {
              case _@("audio/mpeg" | "application/octet-stream") =>
                Failure(new EngineException(s"Invalid MIME-type '$mime' of '$url'"))
              case _ =>
                Failure(new EngineException(s"Unexpected MIME-type '$mime' of '$url'"))
            }
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
  private def headCheckFILE(url: String): Try[HeadResult] = Try {

    val path = Paths.get(url)
    val mimeType = java.nio.file.Files.probeContentType(path)
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
  }

  def fetchContent(url: String, encoding: Option[String]): Try[String] = {
    if (url.startsWith("http://") || url.startsWith("https://")) {
      fetchContentHTTP(url, encoding)
    } else if (url.startsWith("file:///")) {
      fetchContentFILE(url, encoding)
    } else {
      Failure(new IllegalArgumentException("URL points neither to local nor remote resource : " + url))
    }
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

  private def fetchContentHTTP(url: String, encoding: Option[String]): Try[String] = sendGetRequest(url, encoding)
    .flatMap { response =>
      if (response.isSuccess) {
        Success(response)
      } else {
        Failure(new EngineException(s"Download resulted in a non-success response code : ${response.code}"))
      }
    }
    .flatMap { response =>
      response.contentType
        .map(_.split(";")(0).trim) // get the mime type
        .filter(!isValidMime(_))
        .map(mimeType => Failure(new EngineException(s"Aborted before downloading a file with invalid MIME-type : '$mimeType'")))
        .getOrElse(Success(response))
    }
    .flatMap { response =>
      response.contentLength
        .filter(_ > downloadMaxBytes)
        .map(cl => Failure(new EngineException(s"Refusing to download resource because content length exceeds maximum: $cl > $downloadMaxBytes")))
        .getOrElse(Success(response))
    }
    .flatMap { response =>
      response.body match {
        case Left(error) => Failure(new EngineException(s"Error collecting download body; message : $error"))
        case Right(data) => Success(new String(data, encoding.getOrElse("utf-8")))
      }
    }

  private def fetchContentFILE(url: String, encoding: Option[String]): Try[String] = Try {
    Source.fromURL(url).getLines.mkString
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

  private def isValidMime(mime: String): Boolean = {
    mime match {
      case "application/rss+xml"      => true // feed
      case "application/xml"          => true // feed
      case "text/xml"                 => true // feed
      case "text/html"                => true // website
      case "text/plain"               => true // might be ok and might be not -> will have to check manually
      case "none/none"                => true // might be ok and might be not -> will have to check manually
      case "application/octet-stream" => true // some sites use this, but might also be used for media files
      case _                          => false
    }
  }

}
