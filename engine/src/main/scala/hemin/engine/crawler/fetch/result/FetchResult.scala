package hemin.engine.crawler.fetch.result

case class FetchResult (
  data: Array[Byte],
  encoding: String,
  mime: Option[String],
)
