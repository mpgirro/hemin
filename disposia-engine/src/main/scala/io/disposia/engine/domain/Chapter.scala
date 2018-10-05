package io.disposia.engine.domain
import io.disposia.engine.catalog.repository.BsonConversion
import io.disposia.engine.util.mapper.reduce
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

case class Chapter(
                       id: Option[String]        = None,
                       episodeId: Option[String] = None,
                       start: Option[String]     = None,
                       title: Option[String]     = None,
                       href: Option[String]      = None,
                       image: Option[String]     = None
                     ) {

  def update(patch: Chapter): Chapter = {
    Option(patch) match {
      case None => this
      case Some(p) =>
        Chapter(
          id        = reduce(this.id, p.id),
          episodeId = reduce(this.episodeId, p.episodeId),
          start     = reduce(this.start, p.start),
          title     = reduce(this.title, p.title),
          href      = reduce(this.href, p.href),
          image     = reduce(this.image, p.image),
        )
    }
  }

}
