package hemin.engine.crawler.fetch.impl

import java.nio.file.{Path, Paths}

import com.typesafe.scalalogging.Logger
import hemin.engine.HeminException
import hemin.engine.crawler.fetch.result.{FetchResult, HeadResult}

import scala.io.Source
import scala.util.Try

class FileFetchImpl
  extends FetchImpl {

  private val log = Logger(getClass)

  override def isSpecificProtocol(url: String): Boolean = url.startsWith("file:///")

  override def close(): Unit = {
    log.debug(s"Closing ${this.getClass.getSimpleName}")
  }

  override protected[this] def specificCheck(url: String, isValidMime: String => Boolean): Try[HeadResult] = Try {
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
      throw new HeminException(s"check failed on MIME : '$mimeType'")
    }
  }

  override protected[this] def specificFetch(url: String, encoding: Option[String], isValidMime: String => Boolean): Try[FetchResult] = Try {
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

    FetchResult(
      data     = data,
      encoding = enc,
      mime     = mime,
    )
  }

}
