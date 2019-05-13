package io.hemin.engine.catalog.repository

import java.time.{LocalDateTime, ZonedDateTime}

import io.hemin.engine.model._
import io.hemin.engine.util.mapper.DateMapper
import reactivemongo.bson._

object BsonConversion {

  def toBsonI(value: Option[Int]): Option[BSONInteger] = value.flatMap(toBsonI)
  def toBsonI(value: Int): Option[BSONInteger] = Option(value).map(BSONInteger)

  def toBsonL(value: Option[Long]): Option[BSONLong] = value.flatMap(toBsonL)
  def toBsonL(value: Long): Option[BSONLong] = Option(value).map(BSONLong)

  def toBsonS(value: Option[String]): Option[BSONString] = value.flatMap(toBsonS)
  def toBsonS(value: String): Option[BSONString] = Option(value).map(BSONString)

  def toBsonB(value: Option[Boolean]): Option[BSONBoolean] = value.flatMap(toBsonB)
  def toBsonB(value: Boolean): Option[BSONBoolean] = Option(value).map(BSONBoolean)

  def toBsonD(value: Option[LocalDateTime]): Option[BSONDateTime] = value.flatMap(toBsonD)
  def toBsonD(value: LocalDateTime): Option[BSONDateTime] = DateMapper.asMilliseconds(value).map(BSONDateTime)


  def asInt(key: String)(implicit bson: BSONDocument): Option[Int] = bson
    .getAs[BSONNumberLike](key)
    .map(_.toInt)
  def asLong(key: String)(implicit bson: BSONDocument): Option[Long] = bson
    .getAs[BSONNumberLike](key)
    .map(_.toLong)
  def asString(key: String)(implicit bson: BSONDocument): Option[String] = bson.getAs[String](key)
  def asBoolean(key: String)(implicit bson: BSONDocument): Option[Boolean] = bson.getAs[Boolean](key)
  def asLocalDateTime(key: String)(implicit bson: BSONDocument): Option[LocalDateTime] = bson
    .getAs[BSONDateTime](key)
    .map(_.value)
    .flatMap(DateMapper.asLocalDateTime)

  def asStringSet(key: String)(implicit bson: BSONDocument): Option[Set[String]] = bson.getAs[Set[String]](key)


  implicit object LocalDateTimeWriter extends BSONWriter[LocalDateTime,BSONDateTime] {
    def write(value: LocalDateTime): BSONDateTime = BSONDateTime(DateMapper.asMilliseconds(value).get)
  }

  implicit object LocalDateTimeReader extends BSONReader[BSONDateTime,LocalDateTime] {
    def read(dt: BSONDateTime): LocalDateTime = DateMapper.asLocalDateTime(dt.value).get
  }

  implicit object ZonedDateTimeWriter extends BSONWriter[ZonedDateTime,BSONDateTime] {
    def write(value: ZonedDateTime): BSONDateTime = BSONDateTime(DateMapper.asMilliseconds(value).get)
  }

  implicit object ZonedDateTimeReader extends BSONReader[BSONDateTime,ZonedDateTime] {
    def read(dt: BSONDateTime): ZonedDateTime = DateMapper.asZonedDateTime(dt.value).get
  }

  implicit object FeedStatusWriter extends BSONWriter[FeedStatus,BSONString] {
    def write(value: FeedStatus): BSONString = BSONString(value.entryName)
  }

  implicit object FeedStatusReader extends BSONReader[BSONString,FeedStatus] {
    def read(status: BSONString): FeedStatus = FeedStatus.withName(status.value)
  }

  implicit val episodeEnclosureWriter: BSONDocumentWriter[EpisodeEnclosure] = Macros.writer[EpisodeEnclosure]
  implicit val episodeEnclosureReader: BSONDocumentReader[EpisodeEnclosure] = Macros.reader[EpisodeEnclosure]

  implicit val episodeItunesWriter: BSONDocumentWriter[EpisodeItunes] = Macros.writer[EpisodeItunes]
  implicit val episodeItunesReader: BSONDocumentReader[EpisodeItunes] = Macros.reader[EpisodeItunes]

  implicit val episodeRegistrationWriter: BSONDocumentWriter[EpisodeRegistration] = Macros.writer[EpisodeRegistration]
  implicit val episodeRegistrationReader: BSONDocumentReader[EpisodeRegistration] = Macros.reader[EpisodeRegistration]

  implicit val podcastFeedpressWriter: BSONDocumentWriter[PodcastFeedpress] = Macros.writer[PodcastFeedpress]
  implicit val podcastFeedpressReader: BSONDocumentReader[PodcastFeedpress] = Macros.reader[PodcastFeedpress]

  implicit val podcastFyydWriter: BSONDocumentWriter[PodcastFyyd] = Macros.writer[PodcastFyyd]
  implicit val podcastFyydReader: BSONDocumentReader[PodcastFyyd] = Macros.reader[PodcastFyyd]

  implicit val podcastItunesWriter: BSONDocumentWriter[PodcastItunes] = Macros.writer[PodcastItunes]
  implicit val podcastItunesReader: BSONDocumentReader[PodcastItunes] = Macros.reader[PodcastItunes]

  implicit val podcastRegistrationWriter: BSONDocumentWriter[PodcastRegistration] = Macros.writer[PodcastRegistration]
  implicit val podcastRegistrationReader: BSONDocumentReader[PodcastRegistration] = Macros.reader[PodcastRegistration]

  implicit val atomLinkWriter: BSONDocumentWriter[AtomLink] = Macros.writer[AtomLink]
  implicit val atomLinkReader: BSONDocumentReader[AtomLink] = Macros.reader[AtomLink]

  implicit val atomWriter: BSONDocumentWriter[Atom] = Macros.writer[Atom]
  implicit val atomReader: BSONDocumentReader[Atom] = Macros.reader[Atom]

  implicit val personWriter: BSONDocumentWriter[Person] = Macros.writer[Person]
  implicit val personReader: BSONDocumentReader[Person] = Macros.reader[Person]

  implicit val personaWriter: BSONDocumentWriter[Persona] = Macros.writer[Persona]
  implicit val personaReader: BSONDocumentReader[Persona] = Macros.reader[Persona]

  val podcastWriter: BSONDocumentWriter[Podcast] = Macros.writer[Podcast]
  val podcastReader: BSONDocumentReader[Podcast] = Macros.reader[Podcast]

  val chapterWriter: BSONDocumentWriter[Chapter] = Macros.writer[Chapter]
  val chapterReader: BSONDocumentReader[Chapter] = Macros.reader[Chapter]

  // ensure implicit versions in this scope in order to have chapters written and read from/to MongoDB
  implicit val implicitChapterWriter: BSONDocumentWriter[Chapter] = chapterWriter
  implicit val implicitChapterReader: BSONDocumentReader[Chapter] = chapterReader

  val episodeWriter: BSONDocumentWriter[Episode] = Macros.writer[Episode]
  val episodeReader: BSONDocumentReader[Episode] = Macros.reader[Episode]

  val feedWriter: BSONDocumentWriter[Feed] = Macros.writer[Feed]
  val feedReader: BSONDocumentReader[Feed] = Macros.reader[Feed]

  val imageWriter: BSONDocumentWriter[Image] = Macros.writer[Image]
  val imageReader: BSONDocumentReader[Image] = Macros.reader[Image]

}
