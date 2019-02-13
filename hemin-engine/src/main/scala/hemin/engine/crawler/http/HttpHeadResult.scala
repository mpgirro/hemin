package hemin.engine.crawler.http

final case class HttpHeadResult(
  statusCode: Int,
  location: Option[String],
  mimeType: Option[String],
  contentEncoding: Option[String],
  eTag: Option[String],
  lastModified: Option[String],
)
