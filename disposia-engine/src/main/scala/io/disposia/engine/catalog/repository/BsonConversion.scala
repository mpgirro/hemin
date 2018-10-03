package io.disposia.engine.catalog.repository

import java.time.LocalDateTime

import io.disposia.engine.domain._
import io.disposia.engine.newdomain.Image
import io.disposia.engine.mapper.DateMapper
import reactivemongo.bson.{BSONArray, BSONBoolean, BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONInteger, BSONLong, BSONNumberLike, BSONString, BSONValue}

import scala.collection.JavaConverters._


object BsonConversion {

  def toBson(value: Int): Option[BSONInteger] = Option(value).flatMap(i => Option(BSONInteger(i)))
  def toBson(value: Long): Option[BSONLong] = Option(value).flatMap(l => Option(BSONLong(l)))
  def toBson(value: String): Option[BSONString] = Option(value).flatMap(s => Option(BSONString(s)))
  def toBson(value: Boolean): Option[BSONBoolean] = Option(value).flatMap(b => Option(BSONBoolean(b)))
  def toBson(value: LocalDateTime): Option[BSONDateTime] =
    Option(value).flatMap(d => Option(BSONDateTime(DateMapper.INSTANCE.asMilliseconds(d))))

  //def toBson(values: Iterable[Chapter]): BSONArray = BSONArray.apply(values.map(c => ChapterWriter.write(c)))

  def toBsonI(value: Option[Int]): Option[BSONInteger] = value.flatMap(toBson)
  def toBsonL(value: Option[Long]): Option[BSONLong] = value.flatMap(toBson)
  def toBsonS(value: Option[String]): Option[BSONString] = value.flatMap(toBson)
  def toBsonB(value: Option[Boolean]): Option[BSONBoolean] = value.flatMap(toBson)
  def toBsonD(value: Option[LocalDateTime]): Option[BSONDateTime] = value.flatMap(toBson)

  def toDocument(map: Map[String, Option[BSONValue]]): BSONDocument =
    BSONDocument.apply(map.collect { case (key, Some(value)) => key -> value })

  def asInt(key: String)(implicit bson: BSONDocument): Option[Int] = bson.getAs[BSONNumberLike](key).map(_.toInt)
  def asLong(key: String)(implicit bson: BSONDocument): Option[Long] = bson.getAs[BSONNumberLike](key).map(_.toLong)
  def asString(key: String)(implicit bson: BSONDocument): Option[String] = bson.getAs[String](key)
  def asBoolean(key: String)(implicit bson: BSONDocument): Option[Boolean] = bson.getAs[Boolean](key)
  def asLocalDateTime(key: String)(implicit bson: BSONDocument): Option[LocalDateTime] =
    bson.getAs[BSONDateTime](key).map(dt => DateMapper.INSTANCE.asLocalDateTime(dt.value))

  def asChapterList(key: String)(implicit bson: BSONDocument): Option[List[Chapter]] = bson.getAs[List[Chapter]](key)

  private implicit val podcastWriter: BSONDocumentWriter[Podcast] = PodcastWriter
  private implicit val podcastReader: BSONDocumentReader[Podcast] = PodcastReader
  private implicit val episodeWriter: BSONDocumentWriter[Episode] = EpisodeWriter
  private implicit val episodeReader: BSONDocumentReader[Episode] = EpisodeReader
  private implicit val feedWriter: BSONDocumentWriter[Feed] = FeedWriter
  private implicit val feedReader: BSONDocumentReader[Feed] = FeedReader
  private implicit val chapterWriter: BSONDocumentWriter[Chapter] = ChapterWriter
  private implicit val chapterReader: BSONDocumentReader[Chapter] = ChapterReader
  private implicit val imageWriter: BSONDocumentWriter[Image] = ImageWriter
  private implicit val imageReader: BSONDocumentReader[Image] = ImageReader

  object PodcastReader extends BSONDocumentReader[Podcast] {
    override def read(bson: BSONDocument): Podcast = {
      implicit val implicitBson: BSONDocument = bson
      val p = ImmutablePodcast.builder()

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

  object PodcastWriter extends BSONDocumentWriter[Podcast] {
    override def write(p: Podcast): BSONDocument =
      toDocument(Map(
        "id"                    -> toBson(p.getId), // TODO remove; no rel. DB
        //"exo"                   -> toBson(p.getExo),
        "title"                 -> toBson(p.getTitle),
        "link"                  -> toBson(p.getLink),
        "description"           -> toBson(p.getDescription),
        "pubDate"               -> toBson(p.getPubDate),
        "lastBuildDate"         -> toBson(p.getLastBuildDate),
        "language"              -> toBson(p.getLanguage),
        "generator"             -> toBson(p.getGenerator),
        "copyright"             -> toBson(p.getCopyright),
        "docs"                  -> toBson(p.getDocs),
        "managingEditor"        -> toBson(p.getManagingEditor),
        "image"                 -> toBson(p.getImage),
        "itunesSummary"         -> toBson(p.getItunesSummary),
        "itunesAuthor"          -> toBson(p.getItunesAuthor),
        "itunesKeywords"        -> toBson(p.getItunesKeywords),
        //"itunesCategorie"       -> p.getItunesCategories.asScala.mkString("|"), // TODO collection!
        "itunesExplicit"        -> toBson(p.getItunesExplicit),
        "itunesBlock"           -> toBson(p.getItunesBlock),
        "itunesType"            -> toBson(p.getItunesType),
        "itunesOwnerName"       -> toBson(p.getItunesOwnerName),
        "itunesOwnerEmail"      -> toBson(p.getItunesOwnerEmail),
        "feedpressLocale"       -> toBson(p.getFeedpressLocale),
        "fyydVerify"            -> toBson(p.getFyydVerify),
        "episodeCount"          -> toBson(p.getEpisodeCount), // TODO will ich das feld hier ausbauen?
        "registrationTimestamp" -> toBson(p.getRegistrationTimestamp),
        "registrationComplete"  -> toBson(p.getRegistrationComplete)
      ))
  }

  object EpisodeReader extends BSONDocumentReader[Episode] {
    override def read(bson: BSONDocument): Episode = {
      implicit val implicitBson: BSONDocument = bson
      val e = ImmutableEpisode.builder()

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
      asChapterList("chapters").foreach { cs => e.setChapters(cs.asJava) }

      e.create()
    }
  }

  object EpisodeWriter extends BSONDocumentWriter[Episode] {
    override def write(e: Episode): BSONDocument = {
      val doc: BSONDocument = toDocument(Map(
        "id"                    -> toBson(e.getId),        // TODO remove; no rel. DB
        "podcastId"             -> toBson(e.getPodcastId), // TODO remove; no rel. DB
        //"exo"                   -> toBson(e.getExo),
        //"podcastExo"            -> toBson(e.getPodcastExo),
        "podcastTitle"          -> toBson(e.getPodcastTitle),
        "title"                 -> toBson(e.getTitle),
        "link"                  -> toBson(e.getLink),
        "pubDate"               -> toBson(e.getPubDate),
        "guid"                  -> toBson(e.getGuid),
        "guidIsPermalink"       -> toBson(e.getGuidIsPermaLink),
        "description"           -> toBson(e.getDescription),
        "image"                 -> toBson(e.getImage),
        "itunesDuration"        -> toBson(e.getItunesDuration),
        "itunesSubtitle"        -> toBson(e.getItunesSubtitle),
        "itunesAuthor"          -> toBson(e.getItunesAuthor),
        "itunesSummary"         -> toBson(e.getItunesSummary),
        "itunesSeason"          -> toBson(e.getItunesSeason),
        "itunesEpisode"         -> toBson(e.getItunesEpisode),
        "itunesEpisodeType"     -> toBson(e.getItunesEpisodeType),
        "enclosureUrl"          -> toBson(e.getEnclosureUrl),
        "enclosureLength"       -> toBson(e.getEnclosureLength),
        "enclosureType"         -> toBson(e.getEnclosureType),
        "contentEncoded"        -> toBson(e.getContentEncoded),
        "registrationTimestamp" -> toBson(e.getRegistrationTimestamp),
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

  object FeedReader extends BSONDocumentReader[Feed] {
    override def read(bson: BSONDocument): Feed = {
      implicit val implicitBson: BSONDocument = bson
      val f = ImmutableFeed.builder()

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

  object FeedWriter extends BSONDocumentWriter[Feed] {
    override def write(f: Feed): BSONDocument =
      toDocument(Map(
        "id"                    -> toBson(f.getId),
        "podcastId"             -> toBson(f.getPodcastId),
        //"exo"                   -> toBson(f.getExo),
        //"podcastExo"            -> toBson(f.getPodcastExo),
        "url"                   -> toBson(f.getUrl),
        "lastChecked"           -> toBson(f.getLastChecked),
        "lastStatus"            -> toBson(f.getLastStatus.getName),
        "registrationTimestamp" -> toBson(f.getRegistrationTimestamp)
      ))
  }

  object ChapterReader extends BSONDocumentReader[Chapter] {
    override def read(bson: BSONDocument): Chapter = {
      implicit val implicitBson: BSONDocument = bson
      val c = ImmutableChapter.builder()

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

  object ChapterWriter extends BSONDocumentWriter[Chapter] {
    override def write(c: Chapter): BSONDocument =
      toDocument(Map(
        "id"         -> toBson(c.getId),        // TODO remove; no rel. DB
        "episodeId"  -> toBson(c.getEpisodeId), // TODO remove; no rel. DB
        "start"      -> toBson(c.getStart),
        "title"      -> toBson(c.getTitle),
        "href"       -> toBson(c.getHref),
        "image"      -> toBson(c.getImage),
        //"episodeExo" -> toBson(c.getEpisodeExo)
      ))
  }

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

}
