package hemin.engine.util.mapper

import java.net.URL

import scala.util.Try

object UrlMapper {

  private val blank = " "
  private val empty = ""

  def asString(url: URL): Option[String] = Option(url.toExternalForm)

  // + the URL constructor can throw a java.net.MalformedURLException
  // + the Option.get call can fail also, but handling it cleanly is
  //   just verbose and brings no benefit
  def asUrl(url: String): Try[URL] = Try(Option(url).map(new URL(_)).get)

  def sanitize(url: String): Option[String] = Option(url)
    .map { _
      .replace("<",  empty)
      .replace(">",  empty)
      .replace("\n", empty)
      .replace("\t", empty)
      .replace("\r", empty)
    }
    //.map(URLEncoder.encode(_, "UTF-8"))
    //.map(_.replace("%", "")) // the char `^` breaks Play, even URL encoded

  def keywords(url: String): String = Option(url)
    .map(_.replaceAll("http://",  empty))
    .map(_.replaceAll("HTTP://",  empty))
    .map(_.replaceAll("https://", empty))
    .map(_.replaceAll("HTTPS://", empty))
    .map(_.replaceAll("\\.",      blank))
    .map(_.replaceAll("\\+",      blank))
    .map(_.replaceAll("\\-",      blank))
    .map(_.replaceAll("@",        blank))   // userinfo separator
    .map(_.replaceAll(":",        blank))   // port separator
    .map(_.replaceAll("\\[",      blank))   // host subcomponent left enclosure
    .map(_.replaceAll("\\]",      blank))   // host subcomponent right enclosure
    .map(_.replaceAll("/",        blank))   // path separator
    .map(_.replaceAll("\\?",      blank))   // query separator
    .map(_.replaceAll("#",        blank))   // fragment separator
    .map(_.replaceAll("&",        blank))   // parameter separator
    .map(_.replaceAll("=",        blank))   // parameter name/value separator
    .map(_.replaceAll(";",        blank))   // matrix parameter separator
    .map(_.trim.replaceAll(" +",  blank))   // finally remove all whitespace >1 blank to 1 blank
    .getOrElse("")

}
