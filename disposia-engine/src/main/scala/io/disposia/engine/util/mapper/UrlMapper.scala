package io.disposia.engine.util.mapper

import java.net.{MalformedURLException, URL}

object UrlMapper {

  def asString(url: URL): Option[String] = Option(url.toExternalForm)

  def asUrl(url: String): Option[URL] =
    try
      Option(url).map(u => new URL(u))
    catch {
      case e: MalformedURLException => throw new RuntimeException(e)
    }

  def sanitize(url: String): Option[String] = Option(url)
    .map { _
      .replace("<", "")
      .replace(">", "")
      .replace("\n", "")
      .replace("\t", "")
      .replace("\r", "")
    }

}
