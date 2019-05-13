package io.hemin.engine.crawler.fetch.impl

import com.softwaremill.sttp.{Response, SttpBackend, _}
import com.typesafe.scalalogging.Logger
import io.hemin.engine.crawler.fetch.result.{FetchResult, HeadResult}

import scala.concurrent.duration.FiniteDuration
import scala.util.{Success, Try}

class HttpFetchImpl(downloadTimeout: FiniteDuration,
                    downloadMaxBytes: Long)
  extends FetchImpl {

  private val log = Logger(getClass)

  private implicit val sttpBackend: SttpBackend[Id, Nothing] =
    HttpURLConnectionBackend(options = SttpBackendOptions.connectionTimeout(downloadTimeout))

  override def close(): Unit = {
    log.debug(s"Closing ${this.getClass.getSimpleName}")
    sttpBackend.close()
  }

  override def isSpecificProtocol(url: String): Boolean =
    url.startsWith("http://") || url.startsWith("https://")

  override protected[this] def specificCheck(url: String, isValidMime: String => Boolean): Try[HeadResult] =
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

  override protected[this] def specificFetch(url: String, encoding: Option[String], isValidMime: String => Boolean): Try[FetchResult] =
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
            Success(FetchResult(
              data     = data,
              encoding = enc,
              mime     = mime,
            ))
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

}
