package io.hemin.engine.crawler.http

case class HeadResult (
  statusCode: Int,
  location: Option[String],
  mimeType: Option[String],
  contentEncoding: Option[String],
  eTag: Option[String],
  lastModified: Option[String],
)
