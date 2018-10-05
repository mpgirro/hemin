package io.disposia.engine.domain

import io.disposia.engine.util.mapper.reduce

case class Chapter(
  id: Option[String]        = None,
  episodeId: Option[String] = None,
  start: Option[String]     = None,
  title: Option[String]     = None,
  href: Option[String]      = None,
  image: Option[String]     = None
) {

  def patch(diff: Chapter): Chapter = Option(diff) match {
    case None => this
    case Some(x) =>
      Chapter(
        id        = reduce(this.id, x.id),
        episodeId = reduce(this.episodeId, x.episodeId),
        start     = reduce(this.start, x.start),
        title     = reduce(this.title, x.title),
        href      = reduce(this.href, x.href),
        image     = reduce(this.image, x.image),
      )
  }

}
