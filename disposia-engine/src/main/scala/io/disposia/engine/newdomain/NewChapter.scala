package io.disposia.engine.newdomain
import io.disposia.engine.catalog.repository.BsonConversion
import io.disposia.engine.util.mapper.reduce
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

object NewChapter {
  implicit val bsonWriter: BSONDocumentWriter[NewChapter] = Macros.writer[NewChapter]
  implicit val bsonReader: BSONDocumentReader[NewChapter] = Macros.reader[NewChapter]

  private implicit val bsonDateTimeWriter: BsonConversion.DateReader.type = BsonConversion.DateReader
  private implicit val bsonDateTimeReader: BsonConversion.DateWriter.type = BsonConversion.DateWriter
}

case class NewChapter(
                       id: Option[String]        = None,
                       episodeId: Option[String] = None,
                       start: Option[String]     = None,
                       title: Option[String]     = None,
                       href: Option[String]      = None,
                       image: Option[String]     = None
                     ) {

  def update(patch: NewChapter): NewChapter = {
    Option(patch) match {
      case None => this
      case Some(p) =>
        NewChapter(
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
