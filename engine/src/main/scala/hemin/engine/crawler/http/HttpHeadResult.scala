package hemin.engine.crawler.http

@deprecated("replaced by hemin.engine.crawler.fetch.result classes")
final case class HttpHeadResult(
  statusCode: Int,
  location: Option[String],
  mimeType: Option[String],
  contentEncoding: Option[String],
  eTag: Option[String],
  lastModified: Option[String],
)
