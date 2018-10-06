package io.disposia.engine.catalog.repository

import java.time.LocalDateTime

import io.disposia.engine.domain.{FeedStatus, _}
import io.disposia.engine.domain.episode.{EpisodeEnclosureInfo, EpisodeItunesInfo, EpisodeRegistrationInfo}
import io.disposia.engine.domain.podcast._
import io.disposia.engine.util.mapper.DateMapper
import reactivemongo.bson.{BSONBoolean, BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONInteger, BSONLong, BSONNumberLike, BSONReader, BSONString, BSONValue, BSONWriter, Macros}


object BsonConversion {

  def toBsonI(value: Option[Int]): Option[BSONInteger] = value.flatMap(toBsonI)
  def toBsonI(value: Int): Option[BSONInteger] = Option(value).flatMap(i => Option(BSONInteger(i)))

  def toBsonL(value: Option[Long]): Option[BSONLong] = value.flatMap(toBsonL)
  def toBsonL(value: Long): Option[BSONLong] = Option(value).flatMap(l => Option(BSONLong(l)))

  def toBsonS(value: Option[String]): Option[BSONString] = value.flatMap(toBsonS)
  def toBsonS(value: String): Option[BSONString] = Option(value).flatMap(s => Option(BSONString(s)))

  def toBsonB(value: Option[Boolean]): Option[BSONBoolean] = value.flatMap(toBsonB)
  def toBsonB(value: Boolean): Option[BSONBoolean] = Option(value).flatMap(b => Option(BSONBoolean(b)))

  def toBsonD(value: Option[LocalDateTime]): Option[BSONDateTime] = value.flatMap(toBsonD)
  def toBsonD(value: LocalDateTime): Option[BSONDateTime] = Option(value).flatMap(DateMapper.asMilliseconds).map(BSONDateTime)

  def toDocument(map: Map[String, Option[BSONValue]]): BSONDocument =
    BSONDocument.apply(map.collect { case (key, Some(value)) => key -> value })

  def asInt(key: String)(implicit bson: BSONDocument): Option[Int] = bson.getAs[BSONNumberLike](key).map(_.toInt)
  def asLong(key: String)(implicit bson: BSONDocument): Option[Long] = bson.getAs[BSONNumberLike](key).map(_.toLong)
  def asString(key: String)(implicit bson: BSONDocument): Option[String] = bson.getAs[String](key)
  def asBoolean(key: String)(implicit bson: BSONDocument): Option[Boolean] = bson.getAs[Boolean](key)
  def asLocalDateTime(key: String)(implicit bson: BSONDocument): Option[LocalDateTime] =
    bson.getAs[BSONDateTime](key).flatMap(dt => DateMapper.asLocalDateTime(dt.value))

  def asStringSet(key: String)(implicit bson: BSONDocument): Option[Set[String]] = bson.getAs[Set[String]](key)


  implicit object DateWriter extends BSONWriter[LocalDateTime,BSONDateTime] {
    def write(value: LocalDateTime): BSONDateTime = BSONDateTime(DateMapper.asMilliseconds(value).get)
  }

  implicit object DateReader extends BSONReader[BSONDateTime,LocalDateTime] {
    def read(dt: BSONDateTime): LocalDateTime = DateMapper.asLocalDateTime(dt.value).get
  }

  implicit object FeedStatusWriter extends BSONWriter[FeedStatus,BSONString] {
    def write(value: FeedStatus): BSONString = BSONString(value.getName)
  }

  implicit object FeedStatusReader extends BSONReader[BSONString,FeedStatus] {
    def read(status: BSONString): FeedStatus = FeedStatus.getByName(status.value)
  }

  implicit val implicitEpisodeEnclosureInfoWriter: BSONDocumentWriter[EpisodeEnclosureInfo] = Macros.writer[EpisodeEnclosureInfo]
  implicit val implicitEpisodeEnclosureInfoReader: BSONDocumentReader[EpisodeEnclosureInfo] = Macros.reader[EpisodeEnclosureInfo]

  implicit val implicitEpisodeItunesInfoWriter: BSONDocumentWriter[EpisodeItunesInfo] = Macros.writer[EpisodeItunesInfo]
  implicit val implicitEpisodeItunesInfoReader: BSONDocumentReader[EpisodeItunesInfo] = Macros.reader[EpisodeItunesInfo]

  implicit val implicitEpisodeRegistrationInfoWriter: BSONDocumentWriter[EpisodeRegistrationInfo] = Macros.writer[EpisodeRegistrationInfo]
  implicit val implicitEpisodeRegistrationInfoReader: BSONDocumentReader[EpisodeRegistrationInfo] = Macros.reader[EpisodeRegistrationInfo]

  implicit val implicitPodcastFeedpressInfoWriter: BSONDocumentWriter[PodcastFeedpressInfo] = Macros.writer[PodcastFeedpressInfo]
  implicit val implicitPodcastFeedpressInfoReader: BSONDocumentReader[PodcastFeedpressInfo] = Macros.reader[PodcastFeedpressInfo]

  implicit val implicitPodcastFyydInfoWriter: BSONDocumentWriter[PodcastFyydInfo] = Macros.writer[PodcastFyydInfo]
  implicit val implicitPodcastFyydInfoReader: BSONDocumentReader[PodcastFyydInfo] = Macros.reader[PodcastFyydInfo]

  implicit val implicitPodcastItunesInfoWriter: BSONDocumentWriter[PodcastItunesInfo] = Macros.writer[PodcastItunesInfo]
  implicit val implicitPodcastItunesInfoReader: BSONDocumentReader[PodcastItunesInfo] = Macros.reader[PodcastItunesInfo]

  implicit val implicitPodcastMetadataWriter: BSONDocumentWriter[PodcastMetadata] = Macros.writer[PodcastMetadata]
  implicit val implicitPodcastMetadataReader: BSONDocumentReader[PodcastMetadata] = Macros.reader[PodcastMetadata]

  implicit val implicitPodcastRegistrationInfoWriter: BSONDocumentWriter[PodcastRegistrationInfo] = Macros.writer[PodcastRegistrationInfo]
  implicit val implicitPodcastRegistrationInfoReader: BSONDocumentReader[PodcastRegistrationInfo] = Macros.reader[PodcastRegistrationInfo]


  def podcastWriter: BSONDocumentWriter[Podcast] = Macros.writer[Podcast]
  def podcastReader: BSONDocumentReader[Podcast] = Macros.reader[Podcast]

  def chapterWriter: BSONDocumentWriter[Chapter] = Macros.writer[Chapter]
  def chapterReader: BSONDocumentReader[Chapter] = Macros.reader[Chapter]


  implicit object ChapterListReader extends BSONDocumentReader[List[Chapter]] {
    override def read(bson: BSONDocument): List[Chapter] = {
      //asChapterList("chapters")(bson)
      bson.getAs[List[Chapter]]("chapters").toList.flatten
    }
  }

  implicit object ChapterListWriter extends BSONDocumentWriter[List[Chapter]] {
    override def write(chapters: List[Chapter]): BSONDocument =
      chapters match {
        case Nil => BSONDocument()
        case cs  => BSONDocument("chapters" -> cs.map(c => chapterWriter.write(c)))
      }
  }

  def episodeWriter: BSONDocumentWriter[Episode] = Macros.writer[Episode]
  def episodeReader: BSONDocumentReader[Episode] = Macros.reader[Episode]

  def feedWriter: BSONDocumentWriter[Feed] = Macros.writer[Feed]
  def feedReader: BSONDocumentReader[Feed] = Macros.reader[Feed]

  def imageWriter: BSONDocumentWriter[Image] = Macros.writer[Image]
  def imageReader: BSONDocumentReader[Image] = Macros.reader[Image]

}
