package io.disposia.engine.catalog.repository

import java.time.LocalDateTime

import io.disposia.engine.domain.FeedStatus
import io.disposia.engine.domain._
import io.disposia.engine.domain.episode.{EpisodeEnclosureInfo, EpisodeItunesInfo, EpisodeRegistrationInfo}
import io.disposia.engine.domain.podcast._
import io.disposia.engine.mapper.OldDateMapper
import reactivemongo.bson.{BSONArray, BSONBoolean, BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONInteger, BSONLong, BSONNumberLike, BSONReader, BSONString, BSONValue, BSONWriter, Macros}

import scala.collection.JavaConverters._


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
  def toBsonD(value: LocalDateTime): Option[BSONDateTime] =
    Option(value).flatMap(d => Option(BSONDateTime(OldDateMapper.INSTANCE.asMilliseconds(d))))

  //def toBson(values: Iterable[OldChapter]): BSONArray = BSONArray.apply(values.map(c => ChapterWriter.write(c)))

  def toDocument(map: Map[String, Option[BSONValue]]): BSONDocument =
    BSONDocument.apply(map.collect { case (key, Some(value)) => key -> value })

  def asInt(key: String)(implicit bson: BSONDocument): Option[Int] = bson.getAs[BSONNumberLike](key).map(_.toInt)
  def asLong(key: String)(implicit bson: BSONDocument): Option[Long] = bson.getAs[BSONNumberLike](key).map(_.toLong)
  def asString(key: String)(implicit bson: BSONDocument): Option[String] = bson.getAs[String](key)
  def asBoolean(key: String)(implicit bson: BSONDocument): Option[Boolean] = bson.getAs[Boolean](key)
  def asLocalDateTime(key: String)(implicit bson: BSONDocument): Option[LocalDateTime] =
    bson.getAs[BSONDateTime](key).map(dt => OldDateMapper.INSTANCE.asLocalDateTime(dt.value))

  /*
  @deprecated("use implicit case class conversion","0.1")
  def asOldChapterList(key: String)(implicit bson: BSONDocument): Option[List[OldChapter]] = bson.getAs[List[OldChapter]](key)
  */

  /*
  def asChapterList(key: String)(implicit bson: BSONDocument): List[Chapter] = {
    bson.getAs[List[Chapter]](key).toList.flatten
  }
  */


  def asStringSet(key: String)(implicit bson: BSONDocument): Option[Set[String]] = bson.getAs[Set[String]](key)

  /*
  private implicit val oldPodcastWriter: BSONDocumentWriter[OldPodcast] = OldPodcastWriter
  private implicit val oldPodcastReader: BSONDocumentReader[OldPodcast] = OldPodcastReader
  private implicit val oldEpisodeWriter: BSONDocumentWriter[OldEpisode] = OldEpisodeWriter
  private implicit val oldEpisodeReader: BSONDocumentReader[OldEpisode] = OldEpisodeReader
  private implicit val oldFeedWriter: BSONDocumentWriter[OldFeed] = OldFeedWriter
  private implicit val oldFeedReader: BSONDocumentReader[OldFeed] = OldFeedReader
  private implicit val oldChapterWriter: BSONDocumentWriter[OldChapter] = OldChapterWriter
  private implicit val oldChapterReader: BSONDocumentReader[OldChapter] = OldChapterReader
  */


  implicit object DateWriter extends BSONWriter[LocalDateTime,BSONDateTime] {
    def write(value: LocalDateTime): BSONDateTime = BSONDateTime(OldDateMapper.INSTANCE.asMilliseconds(value))
  }

  implicit object DateReader extends BSONReader[BSONDateTime,LocalDateTime] {
    def read(dt: BSONDateTime): LocalDateTime = OldDateMapper.INSTANCE.asLocalDateTime(dt.value)
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


  //implicit val implicitChapterListWriter: BSONDocumentWriter[List[Chapter]] = Macros.writer[List[Chapter]]
  //implicit val implicitChapterListReader: BSONDocumentReader[List[Chapter]] = Macros.reader[List[Chapter]]


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

  /*
  private implicit val implicitPodcastWriter: BSONDocumentWriter[Podcast] = podcastWriter
  private implicit val implicitPodcastReader: BSONDocumentReader[Podcast] = podcastReader
  private implicit val implicitEpisodeWriter: BSONDocumentWriter[Episode] = episodeWriter
  private implicit val implicitEpisodeReader: BSONDocumentReader[Episode] = episodeReader
  private implicit val implicitFeedWriter: BSONDocumentWriter[Feed] = feedWriter
  private implicit val implicitFeedReader: BSONDocumentReader[Feed] = feedReader
  private implicit val implicitChapterWriter: BSONDocumentWriter[Chapter] = chapterWriter
  private implicit val implicitChapterReader: BSONDocumentReader[Chapter] = chapterReader
  private implicit val implicitImageWriter: BSONDocumentWriter[Image] = imageWriter
  private implicit val implicitImageReader: BSONDocumentReader[Image] = imageReader
  */

  /*
  @deprecated("use case classes","0.1")
  object OldPodcastReader extends BSONDocumentReader[OldPodcast] {
    override def read(bson: BSONDocument): OldPodcast = {
      implicit val implicitBson: BSONDocument = bson
      val p = ImmutableOldPodcast.builder()

      asString("id").map(p.setId)
      //asString("exo").map(p.setExo)
      asString("title").map(p.setTitle)
      asString("link").map(p.setLink)
      asString("description").map(p.setDescription)
      asLocalDateTime("pubDate").map(p.setPubDate)
      asLocalDateTime("lastBuildDate").map(p.setLastBuildDate)
      asString("language").map(p.setLanguage)
      asString("generator").map(p.setGenerator)
      asString("copyright").map(p.setCopyright)
      asString("docs").map(p.setDocs)
      asString("managingEditor").map(p.setManagingEditor)
      asString("image").map(p.setImage)
      asString("itunesSummary").map(p.setItunesSummary)
      asString("itunesAuthor").map(p.setItunesAuthor)
      asString("itunesKeywords").map(p.setItunesKeywords)
      asString("itunesCategories").map(p.addItunesCategories)
      asBoolean("itunesExplicit").map(p.setItunesExplicit(_))
      asBoolean("itunesBlock").map(p.setItunesBlock(_))
      asString("itunesType").map(p.setItunesType)
      asString("itunesOwnerName").map(p.setItunesOwnerName)
      asString("itunesOwnerEmail").map(p.setItunesOwnerEmail)
      asString("feedpressLocale").map(p.setFeedpressLocale)
      asString("fyydVerify").map(p.setFyydVerify)
      asInt("episodeCount").map(p.setEpisodeCount(_))
      asLocalDateTime("registrationTimestamp").map(p.setRegistrationTimestamp)
      asBoolean("registrationComplete").map(p.setRegistrationComplete(_))

      p.create()
    }
  }
  */

  /*
  @deprecated("use case classes","0.1")
  object OldPodcastWriter extends BSONDocumentWriter[OldPodcast] {
    override def write(p: OldPodcast): BSONDocument =
      toDocument(Map(
        "id"                    -> toBsonS(p.getId), // TODO remove; no rel. DB
        //"exo"                   -> toBson(p.getExo),
        "title"                 -> toBsonS(p.getTitle),
        "link"                  -> toBsonS(p.getLink),
        "description"           -> toBsonS(p.getDescription),
        "pubDate"               -> toBsonD(p.getPubDate),
        "lastBuildDate"         -> toBsonD(p.getLastBuildDate),
        "language"              -> toBsonS(p.getLanguage),
        "generator"             -> toBsonS(p.getGenerator),
        "copyright"             -> toBsonS(p.getCopyright),
        "docs"                  -> toBsonS(p.getDocs),
        "managingEditor"        -> toBsonS(p.getManagingEditor),
        "image"                 -> toBsonS(p.getImage),
        "itunesSummary"         -> toBsonS(p.getItunesSummary),
        "itunesAuthor"          -> toBsonS(p.getItunesAuthor),
        "itunesKeywords"        -> toBsonS(p.getItunesKeywords),
        //"itunesCategorie"       -> p.getItunesCategories.asScala.mkString("|"), // TODO collection!
        "itunesExplicit"        -> toBsonB(p.getItunesExplicit),
        "itunesBlock"           -> toBsonB(p.getItunesBlock),
        "itunesType"            -> toBsonS(p.getItunesType),
        "itunesOwnerName"       -> toBsonS(p.getItunesOwnerName),
        "itunesOwnerEmail"      -> toBsonS(p.getItunesOwnerEmail),
        "feedpressLocale"       -> toBsonS(p.getFeedpressLocale),
        "fyydVerify"            -> toBsonS(p.getFyydVerify),
        "episodeCount"          -> toBsonI(p.getEpisodeCount), // TODO will ich das feld hier ausbauen?
        "registrationTimestamp" -> toBsonD(p.getRegistrationTimestamp),
        "registrationComplete"  -> toBsonB(p.getRegistrationComplete)
      ))
  }
  */

  /*
  @deprecated("use case classes","0.1")
  object OldEpisodeReader extends BSONDocumentReader[OldEpisode] {
    override def read(bson: BSONDocument): OldEpisode = {
      implicit val implicitBson: BSONDocument = bson
      val e = ImmutableOldEpisode.builder()

      asString("id").map(e.setId)
      asString("podcastId").map(e.setPodcastId)
      //asString("exo").map(e.setExo)
      //asString("podcastExo").map(e.setPodcastExo)
      asString("podcastTitle").map(e.setPodcastTitle)
      asString("title").map(e.setTitle)
      asString("link").map(e.setLink)
      asLocalDateTime("pubDate").map(e.setPubDate)
      asString("guid").map(e.setGuid)
      asBoolean("guidIsPermalink").map(e.setGuidIsPermaLink(_))
      asString("description").map(e.setDescription)
      asString("image").map(e.setImage)
      asString("itunesDuration").map(e.setItunesDuration)
      asString("itunesSubtitle").map(e.setItunesSubtitle)
      asString("itunesAuthor").map(e.setItunesAuthor)
      asString("itunesSummary").map(e.setItunesSummary)
      asInt("itunesSeason").map(e.setItunesSeason(_))
      asInt("itunesEpisode").map(e.setItunesEpisode(_))
      asString("itunesEpisodeType").map(e.setItunesEpisodeType)
      asString("enclosureUrl").map(e.setEnclosureUrl)
      asLong("enclosureLength").map(e.setEnclosureLength(_))
      asString("enclosureType").map(e.setEnclosureType)
      asString("contentEncoded").map(e.setContentEncoded)
      asLocalDateTime("registrationTimestamp").map(e.setRegistrationTimestamp)
      asOldChapterList("chapters").foreach { cs => e.setChapters(cs.asJava) }

      e.create()
    }
  }
  */

  /*
  @deprecated("use case classes","0.1")
  object OldEpisodeWriter extends BSONDocumentWriter[OldEpisode] {
    override def write(e: OldEpisode): BSONDocument = {
      val doc: BSONDocument = toDocument(Map(
        "id"                    -> toBsonS(e.getId),        // TODO remove; no rel. DB
        "podcastId"             -> toBsonS(e.getPodcastId), // TODO remove; no rel. DB
        //"exo"                   -> toBson(e.getExo),
        //"podcastExo"            -> toBson(e.getPodcastExo),
        "podcastTitle"          -> toBsonS(e.getPodcastTitle),
        "title"                 -> toBsonS(e.getTitle),
        "link"                  -> toBsonS(e.getLink),
        "pubDate"               -> toBsonD(e.getPubDate),
        "guid"                  -> toBsonS(e.getGuid),
        "guidIsPermalink"       -> toBsonB(e.getGuidIsPermaLink),
        "description"           -> toBsonS(e.getDescription),
        "image"                 -> toBsonS(e.getImage),
        "itunesDuration"        -> toBsonS(e.getItunesDuration),
        "itunesSubtitle"        -> toBsonS(e.getItunesSubtitle),
        "itunesAuthor"          -> toBsonS(e.getItunesAuthor),
        "itunesSummary"         -> toBsonS(e.getItunesSummary),
        "itunesSeason"          -> toBsonI(e.getItunesSeason),
        "itunesEpisode"         -> toBsonI(e.getItunesEpisode),
        "itunesEpisodeType"     -> toBsonS(e.getItunesEpisodeType),
        "enclosureUrl"          -> toBsonS(e.getEnclosureUrl),
        "enclosureLength"       -> toBsonL(e.getEnclosureLength),
        "enclosureType"         -> toBsonS(e.getEnclosureType),
        "contentEncoded"        -> toBsonS(e.getContentEncoded),
        "registrationTimestamp" -> toBsonD(e.getRegistrationTimestamp),
        /*
        "chapters" -> BSONArray(e.getChapters.asScala.map {
          c => ChapterWriter.write(c)
        }: _*)
        */
      ))
      //BSONDocument("chapters" -> BSONArray(e.getChapters.asScala.map { c => ChapterWriter.write(c)}: _*))
      Option(e.getChapters)
        .map(cs => doc ++ BSONDocument("chapters" -> cs.asScala))
        .getOrElse(doc)
      /*
      if (e.getChapters != null && !e.getChapters.isEmpty) {
        doc ++ BSONDocument("chapters" -> e.getChapters.asScala)
      } else {
        doc
      }
      */
      //doc ++ BSONDocument("chapters" -> BSONArray(e.getChapters.asScala.map { c => ChapterWriter.write(c)}: _*))
    }
  }
  */

  /*
  @deprecated("use case classes","0.1")
  object OldFeedReader extends BSONDocumentReader[OldFeed] {
    override def read(bson: BSONDocument): OldFeed = {
      implicit val implicitBson: BSONDocument = bson
      val f = ImmutableOldFeed.builder()

      asString("id").map(f.setId)
      asString("podcastId").map(f.setPodcastId)
      //asString("exo").map(f.setExo)
      //asString("podcastExo").map(f.setPodcastExo)
      asString("url").map(f.setUrl)
      asLocalDateTime("lastChecked").map(f.setLastChecked)
      asString("lastStatus").map(status => f.setLastStatus(FeedStatus.getByName(status)))
      asLocalDateTime("registrationTimestamp").map(f.setRegistrationTimestamp)

      f.create()
    }
  }
  */

  /*
  @deprecated("use case classes","0.1")
  object OldFeedWriter extends BSONDocumentWriter[OldFeed] {
    override def write(f: OldFeed): BSONDocument =
      toDocument(Map(
        "id"                    -> toBsonS(f.getId),
        "podcastId"             -> toBsonS(f.getPodcastId),
        //"exo"                   -> toBson(f.getExo),
        //"podcastExo"            -> toBson(f.getPodcastExo),
        "url"                   -> toBsonS(f.getUrl),
        "lastChecked"           -> toBsonD(f.getLastChecked),
        "lastStatus"            -> toBsonS(f.getLastStatus.getName),
        "registrationTimestamp" -> toBsonD(f.getRegistrationTimestamp)
      ))
  }
  */

  /*
  @deprecated("use case classes","0.1")
  object OldChapterReader extends BSONDocumentReader[OldChapter] {
    override def read(bson: BSONDocument): OldChapter = {
      implicit val implicitBson: BSONDocument = bson
      val c = ImmutableOldChapter.builder()

      asString("id").map(c.setId)
      asString("episodeId").map(c.setEpisodeId)
      asString("start").map(c.setStart)
      asString("title").map(c.setTitle)
      asString("href").map(c.setHref)
      asString("image").map(c.setImage)
     // asString("episodeExo").map(c.setEpisodeExo)

      c.create()
    }
  }
  */

  /*
  @deprecated("use case classes","0.1")
  object OldChapterWriter extends BSONDocumentWriter[OldChapter] {
    override def write(c: OldChapter): BSONDocument =
      toDocument(Map(
        "id"         -> toBsonS(c.getId),        // TODO remove; no rel. DB
        "episodeId"  -> toBsonS(c.getEpisodeId), // TODO remove; no rel. DB
        "start"      -> toBsonS(c.getStart),
        "title"      -> toBsonS(c.getTitle),
        "href"       -> toBsonS(c.getHref),
        "image"      -> toBsonS(c.getImage),
        //"episodeExo" -> toBson(c.getEpisodeExo)
      ))
  }
  */

  // # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
  //  # # # # # # # # # # # # # # # # # # # # # # # # # # # #


  /*
  object ImageReader extends BSONDocumentReader[Image] {
    override def read(bson: BSONDocument): Image = {
      implicit val implicitBson: BSONDocument = bson
      Image(
        id          = asString("id"),
        associateId = asString("associateId"),
        //data =  // TODO implement!
        hash        = asString("hash"),
        name        = asString("name"),
        contentType = asString("contentType"),
        size        = asLong("size"),
        createdAt   = asLocalDateTime("createdAt")
      )
    }
  }

  object ImageWriter extends BSONDocumentWriter[Image] {
    override def write(i: Image): BSONDocument =
      toDocument(Map(
        "id"          -> toBsonS(i.id),
        "associateId" -> toBsonS(i.associateId),
        //"data"        -> toBson(i.data), // TODO implement!
        "hash"        -> toBsonS(i.hash),
        "name"        -> toBsonS(i.name),
        "contentType" -> toBsonS(i.contentType),
        "size"        -> toBsonL(i.size),
        "createdAt"   -> toBsonD(i.createdAt)
      ))
  }

  object PodcastReader extends BSONDocumentReader[NewPodcast] {
    override def read(bson: BSONDocument): NewPodcast = {
      implicit val implicitBson: BSONDocument = bson
      NewPodcast(
        id          = asString("id"),
        title       = asString("title"),
        link        = asString("link"),
        description = asString("description"),
        pubDate     = asLocalDateTime("pubDate"),
        meta = PodcastMetadata(
          lastBuildDate  = asLocalDateTime("lastBuildDate"),
          language       = asString("language"),
          generator      = asString("generator"),
          copyright      = asString("copyright"),
          docs           = asString("docs"),
          managingEditor = asString("managingEditor"),
        ),
        registration = PodcastRegistrationInfo(
          timestamp  = asLocalDateTime("registrationTimestamp"),
          complete   = asBoolean("registrationComplete"),
        ),
        itunes = PodcastItunesInfo(
          summary     = asString("itunesSummary"),
          author      = asString("itunesAuthor"),
          keywords    = asString("itunesKeywords"),
          categories  = asStringSet("itunesCategories"),
          explicit    = asBoolean("itunesExplicit"),
          block       = asBoolean("itunesBlock"),
          podcastType = asString("itunesType"),
          ownerName   = asString("itunesOwnerName"),
          ownerEmail  = asString("itunesOwnerEmail"),
        ),
        feedpress = PodcastFeedpressInfo(
          locale = asString("feedpressLocale")
        ),
        fyyd = PodcastFyydInfo(
          verify = asString("fyydVerify")
        )
      )
    }
  }

  object PodcastWriter extends BSONDocumentWriter[NewPodcast] {
    override def write(p: NewPodcast): BSONDocument =
      toDocument(Map(
        "id"                    -> toBsonS(p.id),
        //"exo"                   -> toBson(p.getExo),
        "title"                 -> toBsonS(p.title),
        "link"                  -> toBsonS(p.link),
        "description"           -> toBsonS(p.description),
        "pubDate"               -> toBsonD(p.pubDate),
        "lastBuildDate"         -> toBsonD(p.meta.lastBuildDate),
        "language"              -> toBsonS(p.meta.language),
        "generator"             -> toBsonS(p.meta.generator),
        "copyright"             -> toBsonS(p.meta.copyright),
        "docs"                  -> toBsonS(p.meta.docs),
        "managingEditor"        -> toBsonS(p.meta.managingEditor),
        "image"                 -> toBsonS(p.image),
        "itunesSummary"         -> toBsonS(p.itunes.summary),
        "itunesAuthor"          -> toBsonS(p.itunes.author),
        "itunesKeywords"        -> toBsonS(p.itunes.keywords),
        //"itunesCategorie"       -> p.getItunesCategories.asScala.mkString("|"), // TODO collection!
        "itunesExplicit"        -> toBsonB(p.itunes.explicit),
        "itunesBlock"           -> toBsonB(p.itunes.block),
        "itunesType"            -> toBsonS(p.itunes.podcastType),
        "itunesOwnerName"       -> toBsonS(p.itunes.ownerName),
        "itunesOwnerEmail"      -> toBsonS(p.itunes.ownerEmail),
        "feedpressLocale"       -> toBsonS(p.feedpress.locale),
        "fyydVerify"            -> toBsonS(p.fyyd.verify),
        "registrationTimestamp" -> toBsonD(p.registration.timestamp),
        "registrationComplete"  -> toBsonB(p.registration.complete)
      ))
  }

  object EpisodeReader extends BSONDocumentReader[NewEpisode] {
    override def read(bson: BSONDocument): NewEpisode = {
      implicit val implicitBson: BSONDocument = bson
      NewEpisode(
        id              = asString("id"),
        podcastId       = asString("podcastId"),
        podcastTitle    = asString("podcastTitle"),
        title           = asString("title"),
        link            = asString("link"),
        pubDate         = asLocalDateTime("pubDate"),
        description     = asString("description"),
        image           = asString("image"),
        guid            = asString("guid"),
        guidIsPermalink = asBoolean("guidIsPermalink"),
        itunes = EpisodeItunesInfo(
          duration    = asString("itunesDuration"),
          subtitle    = asString("itunesSubtitle"),
          author      = asString("itunesAuthor"),
          summary     = asString("itunesSummary"),
          season      = asInt("itunesSeason"),
          episode     = asInt("itunesEpisode"),
          episodeType = asString("itunesEpisodeType"),
        ),
        enclosure = EpisodeEnclosureInfo(
          url    = asString("enclosureUrl"),
          length = asLong("enclosureLength"),
          typ    = asString("enclosureType"),
        ),
        contentEncoded = asString("contentEncoded"),
        registration = EpisodeRegistrationInfo(
          timestamp  = asLocalDateTime("registrationTimestamp"),
        ),
        chapters = asNewChapterList("chapters") // TODO
      )
    }
  }

  object EpisodeWriter extends BSONDocumentWriter[NewEpisode] {
    override def write(e: NewEpisode): BSONDocument = {
      val doc: BSONDocument = toDocument(Map(
        "id"                    -> toBsonS(e.id),        // TODO remove; no rel. DB
        "podcastId"             -> toBsonS(e.podcastId), // TODO remove; no rel. DB
        //"exo"                   -> toBson(e.getExo),
        //"podcastExo"            -> toBson(e.getPodcastExo),
        "podcastTitle"          -> toBsonS(e.podcastTitle),
        "title"                 -> toBsonS(e.title),
        "link"                  -> toBsonS(e.link),
        "pubDate"               -> toBsonD(e.pubDate),
        "guid"                  -> toBsonS(e.guid),
        "guidIsPermalink"       -> toBsonB(e.guidIsPermalink),
        "description"           -> toBsonS(e.description),
        "image"                 -> toBsonS(e.image),
        "itunesDuration"        -> toBsonS(e.itunes.duration),
        "itunesSubtitle"        -> toBsonS(e.itunes.subtitle),
        "itunesAuthor"          -> toBsonS(e.itunes.author),
        "itunesSummary"         -> toBsonS(e.itunes.summary),
        "itunesSeason"          -> toBsonI(e.itunes.season),
        "itunesEpisode"         -> toBsonI(e.itunes.episode),
        "itunesEpisodeType"     -> toBsonS(e.itunes.episodeType),
        "enclosureUrl"          -> toBsonS(e.enclosure.url),
        "enclosureLength"       -> toBsonL(e.enclosure.length),
        "enclosureType"         -> toBsonS(e.enclosure.typ),
        "contentEncoded"        -> toBsonS(e.contentEncoded),
        "registrationTimestamp" -> toBsonD(e.registration.timestamp),

      ))
    }
  }

  object FeedReader extends BSONDocumentReader[NewFeed] {
    override def read(bson: BSONDocument): NewFeed = {
      implicit val implicitBson: BSONDocument = bson
      val f = ImmutableOldFeed.builder()

      asString("id").map(f.setId)
      asString("podcastId").map(f.setPodcastId)
      //asString("exo").map(f.setExo)
      //asString("podcastExo").map(f.setPodcastExo)
      asString("url").map(f.setUrl)
      asLocalDateTime("lastChecked").map(f.setLastChecked)
      asString("lastStatus").map(status => f.setLastStatus(FeedStatus.getByName(status)))
      asLocalDateTime("registrationTimestamp").map(f.setRegistrationTimestamp)

      f.create()
    }
  }

  object FeedWriter extends BSONDocumentWriter[NewFeed] {
    override def write(f: NewFeed): BSONDocument =
      toDocument(Map(
        "id"                    -> toBsonS(f.getId),
        "podcastId"             -> toBsonS(f.getPodcastId),
        //"exo"                   -> toBson(f.getExo),
        //"podcastExo"            -> toBson(f.getPodcastExo),
        "url"                   -> toBsonS(f.getUrl),
        "lastChecked"           -> toBsonD(f.getLastChecked),
        "lastStatus"            -> toBsonS(f.getLastStatus.getName),
        "registrationTimestamp" -> toBsonD(f.getRegistrationTimestamp)
      ))
  }

  object ChapterReader extends BSONDocumentReader[NewChapter] {
    override def read(bson: BSONDocument): NewChapter = {
      implicit val implicitBson: BSONDocument = bson
      val c = ImmutableOldChapter.builder()

      asString("id").map(c.setId)
      asString("episodeId").map(c.setEpisodeId)
      asString("start").map(c.setStart)
      asString("title").map(c.setTitle)
      asString("href").map(c.setHref)
      asString("image").map(c.setImage)
      // asString("episodeExo").map(c.setEpisodeExo)

      c.create()
    }
  }

  object ChapterWriter extends BSONDocumentWriter[NewChapter] {
    override def write(c: NewChapter): BSONDocument =
      toDocument(Map(
        "id"         -> toBsonS(c.getId),        // TODO remove; no rel. DB
        "episodeId"  -> toBsonS(c.getEpisodeId), // TODO remove; no rel. DB
        "start"      -> toBsonS(c.getStart),
        "title"      -> toBsonS(c.getTitle),
        "href"       -> toBsonS(c.getHref),
        "image"      -> toBsonS(c.getImage),
        //"episodeExo" -> toBson(c.getEpisodeExo)
      ))
  }
  */

}
