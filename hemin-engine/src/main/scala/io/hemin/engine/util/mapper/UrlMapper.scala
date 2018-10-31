package io.hemin.engine.util.mapper

import java.net.URL

import scala.util.Try

object UrlMapper {

  def asString(url: URL): Option[String] = Option(url.toExternalForm)

  // + the URL constructor can throw a java.net.MalformedURLException
  // + the Option.get call can fail also, but handling it cleanly is
  //   just verbose and brings no benefit
  def asUrl(url: String): Try[URL] = Try(Option(url).map(new URL(_)).get)

  def sanitize(url: String): Option[String] = Option(url)
    .map { _
      .replace("<", "")
      .replace(">", "")
      .replace("\n", "")
      .replace("\t", "")
      .replace("\r", "")
    }
    //.map(URLEncoder.encode(_, "UTF-8"))
    //.map(_.replace("%", "")) // the char `^` breaks Play, even URL encoded

}
