package io.disposia.engine.catalog.mongo

import java.time.LocalDateTime

import com.google.common.collect.Sets
import io.disposia.engine.domain.FeedStatus
import io.disposia.engine.domain.dto._
import io.disposia.engine.mapper.DateMapper
import reactivemongo.bson.{BSONBoolean, BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONInteger, BSONLong, BSONNumberLike, BSONString, BSONValue}

import scala.collection.JavaConverters._


object BsonConversion {

    def toBson(value: Int): Option[BSONInteger] = Option(value).flatMap(i => Option(BSONInteger(i)))
    def toBson(value: Long): Option[BSONLong] = Option(value).flatMap(l => Option(BSONLong(l)))
    def toBson(value: String): Option[BSONString] = Option(value).flatMap(s => Option(BSONString(s)))
    def toBson(value: Boolean): Option[BSONBoolean] = Option(value).flatMap(b => Option(BSONBoolean(b)))
    def toBson(value: LocalDateTime): Option[BSONDateTime] = Option(value)
        .flatMap(d => Option(BSONDateTime(DateMapper.INSTANCE.asMilliseconds(d))))
    def toBson(map: Map[String, Option[BSONValue]]): BSONDocument =
        BSONDocument.apply(map.collect { case (key, Some(value)) => key -> value })

    /*
    def asInt(key: String)(implicit bson: BSONDocument): Int = bson.getAs[BSONNumberLike](key).map(_.toInt).getOrElse(null)
    def asLong(key: String)(implicit bson: BSONDocument): Long = bson.getAs[BSONNumberLike](key).map(_.toLong).getOrElse(null)
    def asString(key: String)(implicit bson: BSONDocument): String = bson.getAs[String](key).getOrElse(null)
    def asBoolean(key: String)(implicit bson: BSONDocument): Boolean = bson.getAs[Boolean](key).getOrElse(null)
    def asLocalDateTime(key: String)(implicit bson: BSONDocument): LocalDateTime =
        bson.getAs[BSONDateTime](key).map(dt => DateMapper.INSTANCE.asLocalDateTime(dt.value)).getOrElse(null)
        */


    def asInt(key: String)(implicit bson: BSONDocument): Option[Int] = bson.getAs[BSONNumberLike](key).map(_.toInt)
    def asLong(key: String)(implicit bson: BSONDocument): Option[Long] = bson.getAs[BSONNumberLike](key).map(_.toLong)
    def asString(key: String)(implicit bson: BSONDocument): Option[String] = bson.getAs[String](key)
    def asBoolean(key: String)(implicit bson: BSONDocument): Option[Boolean] = bson.getAs[Boolean](key)
    def asLocalDateTime(key: String)(implicit bson: BSONDocument): Option[LocalDateTime] =
        bson.getAs[BSONDateTime](key).map(dt => DateMapper.INSTANCE.asLocalDateTime(dt.value))


    /*
    def asInt(key: String, bson: BSONDocument): Option[Int] = bson.getAs[BSONNumberLike](key).map(_.toInt)
    def asLong(key: String, bson: BSONDocument): Option[Long] = bson.getAs[BSONNumberLike](key).map(_.toLong)
    def asString(key: String, bson: BSONDocument): Option[String] = bson.getAs[String](key)
    def asBoolean(key: String, bson: BSONDocument): Option[Boolean] = bson.getAs[Boolean](key)
    def asLocalDateTime(key: String, bson: BSONDocument): Option[LocalDateTime] =
        bson.getAs[BSONDateTime](key).map(dt => DateMapper.INSTANCE.asLocalDateTime(dt.value))
        */

    implicit object PodcastReader extends BSONDocumentReader[Podcast] {
        override def read(bson: BSONDocument): Podcast = {
            implicit val implicitBson: BSONDocument = bson
            val p = ImmutablePodcast.builder()

            asLong("id").map(p.setId(_))
            asString("exo").map(p.setExo)
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

            /*
            val opt: Option[Podcast] = for {
                id                    <- asLong("id") // TODO remove; no rel. DB
                exo                   <- asString("exo")
                title                 <- asString("title")
                link                  <- asString("link")
                description           <- asString("description")
                pubDate               <- asLocalDateTime("pubDate")
                lastBuildDate         <- asLocalDateTime("lastBuildDate")
                language              <- asString("language")
                generator             <- asString("generator")
                copyright             <- asString("copyright")
                docs                  <- asString("docs")
                managingEditor        <- asString("managingEditor")
                image                 <- asString("image")
                itunesSummary         <- asString("itunesSummary")
                itunesAuthor          <- asString("itunesAuthor")
                itunesKeywords        <- asString("itunesKeywords")
                itunesCategories      <- asString("itunesCategories")
                itunesExplicit        <- asBoolean("itunesExplicit")
                itunesBlock           <- asBoolean("itunesBlock")
                itunesType            <- asString("itunesType")
                itunesOwnerName       <- asString("itunesOwnerName")
                itunesOwnerEmail      <- asString("itunesOwnerEmail")
                feedpressLocale       <- asString("feedpressLocale")
                fyydVerify            <- asString("fyydVerify")
                episodeCount          <- asInt("episodeCount")
                registrationTimestamp <- asLocalDateTime("registrationTimestamp")
                registrationComplete  <- asBoolean("registrationComplete")
            } yield p
                .setId(id)
                .setExo(exo)
                .setTitle(title)
                .setLink(link)
                .setPubDate(pubDate)
                .setLastBuildDate(lastBuildDate)
                .setDescription(description)
                .setLanguage(language)
                .setGenerator(generator)
                .setCopyright(copyright)
                .setDocs(docs)
                .setItunesSummary(itunesSummary)
                .setItunesAuthor(itunesAuthor)
                .setItunesKeywords(itunesKeywords)
                .setItunesCategories(
                    Sets.newLinkedHashSet(
                        asJavaCollection(
                            itunesCategories.split("|"))))
                .setItunesExplicit(itunesExplicit)
                .setItunesBlock(itunesBlock)
                .setItunesType(itunesType)
                .setItunesOwnerName(itunesOwnerName)
                .setItunesOwnerEmail(itunesOwnerEmail)
                .setFeedpressLocale(feedpressLocale)
                .setFyydVerify(fyydVerify)
                .setEpisodeCount(episodeCount)
                .setRegistrationTimestamp(registrationTimestamp)
                .setRegistrationComplete(registrationComplete)
                .create()

            opt.get // the Podcast is required (or let throw an exception)
            */

        }
    }

    implicit object PodcastWriter extends BSONDocumentWriter[Podcast] {
        override def write(p: Podcast): BSONDocument = {
            val map: Map[String, Option[BSONValue]] = Map(
                "id"                    -> toBson(p.getId), // TODO remove; no rel. DB
                "exo"                   -> toBson(p.getExo),
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
            )
            toBson(map)
        }

    }

    implicit object EpisodeReader extends BSONDocumentReader[Episode] {
        override def read(bson: BSONDocument): Episode = {
            implicit val implicitBson: BSONDocument = bson
            val e = ImmutableEpisode.builder()

            asLong("id").map(e.setId(_))
            asLong("podcastId").map(e.setPodcastId(_))
            asString("exo").map(e.setExo)
            asString("podcastExo").map(e.setPodcastExo)
            asString("podcastTitle").map(e.setPodcastExo)
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

            e.create()

            /*
            val opt: Option[Episode] = for {
                id                    <- asLong("id") // TODO remove; no rel. DB
                podcastId             <- asLong("podcastId") // TODO remove; no rel. DB
                exo                   <- asString("exo")
                podcastExo            <- asString("podcastExo")
                podcastTitle          <- asString("podcastTitle")
                title                 <- asString("title")
                link                  <- asString("link")
                pubDate               <- asLocalDateTime("pubDate")
                guid                  <- asString("guid")
                guidIsPermalink       <- asBoolean("guidIsPermalink")
                description           <- asString("description")
                image                 <- asString("image")
                itunesDuration        <- asString("itunesDuration")
                itunesSubtitle        <- asString("itunesSubtitle")
                itunesAuthor          <- asString("itunesAuthor")
                itunesSummary         <- asString("itunesSummary")
                itunesSeason          <- asInt("itunesSeason")
                itunesEpisode         <- asInt("itunesEpisode")
                itunesEpisodeType     <- asString("itunesEpisodeType")
                enclosureUrl          <- asString("enclosureUrl")
                enclosureLength       <- asLong("enclosureLength")
                enclosureType         <- asString("enclosureType")
                contentEncoded        <- asString("contentEncoded")
                registrationTimestamp <- asLocalDateTime("registrationTimestamp")
            } yield e
                .setId(id)
                .setPodcastId(podcastId)
                .setExo(exo)
                .setPodcastExo(podcastExo)
                .setPodcastTitle(podcastTitle)
                .setTitle(title)
                .setLink(link)
                .setPubDate(pubDate)
                .setGuid(guid)
                .setGuidIsPermaLink(guidIsPermalink)
                .setDescription(description)
                .setImage(image)
                .setItunesDuration(itunesDuration)
                .setItunesSubtitle(itunesSubtitle)
                .setItunesAuthor(itunesAuthor)
                .setItunesSummary(itunesSummary)
                .setItunesSeason(itunesSeason)
                .setItunesEpisode(itunesEpisode)
                .setItunesEpisodeType(itunesEpisodeType)
                .setEnclosureUrl(enclosureUrl)
                .setEnclosureLength(enclosureLength)
                .setEnclosureType(enclosureType)
                .setContentEncoded(contentEncoded)
                .setRegistrationTimestamp(registrationTimestamp)
                .create()

            opt.get // the Episode is required (or let throw an exception)
            */
        }
    }

    implicit object EpisodeWriter extends BSONDocumentWriter[Episode] {
        override def write(e: Episode): BSONDocument = {
            val map: Map[String, Option[BSONValue]] = Map(
                "id"                    -> toBson(e.getId), // TODO remove; no rel. DB
                "podcastId"             -> toBson(e.getPodcastId), // TODO remove; no rel. DB
                "exo"                   -> toBson(e.getExo),
                "podcastExo"            -> toBson(e.getPodcastExo),
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
            )
            toBson(map)
        }
    }

    implicit object FeedReader extends BSONDocumentReader[Feed] {
        override def read(bson: BSONDocument): Feed = {
            implicit val implicitBson: BSONDocument = bson
            val f = ImmutableFeed.builder()

            asLong("id").map(f.setId(_))
            asLong("podcastId").map(f.setPodcastId(_))
            asString("exo").map(f.setExo)
            asString("podcastExo").map(f.setPodcastExo)
            asString("url").map(f.setUrl)
            asLocalDateTime("lastChecked").map(f.setLastChecked)
            asString("lastStatus").map(status => f.setLastStatus(FeedStatus.getByName(status)))
            asLocalDateTime("registrationTimestamp").map(f.setRegistrationTimestamp)

            f.create()

            /*
            val opt: Option[Feed] = for {
                id                    <- asLong("id") // TODO remove; no rel. DB
                podcastId             <- asLong("podcastId") // TODO remove; no rel. DB
                exo                   <- asString("exo")
                podcastExo            <- asString("podcastExo")
                url                   <- asString("url")
                lastChecked           <- asLocalDateTime("lastChecked")
                lastStatus            <- asString("lastStatus")
                registrationTimestamp <- asLocalDateTime("registrationTimestamp")
            } yield builder
                .setId(id)
                .setPodcastId(podcastId)
                .setExo(exo)
                .setUrl(url)
                .setLastChecked(lastChecked)
                .setLastStatus(FeedStatus.getByName(lastStatus))
                .setRegistrationTimestamp(registrationTimestamp)
                .create()

            opt.get // the Feed is required (or let throw an exception)
            */
        }
    }

    implicit object FeedWriter extends BSONDocumentWriter[Feed] {
        override def write(f: Feed): BSONDocument = {
            val map: Map[String, Option[BSONValue]] = Map(
                "id"                    -> toBson(f.getId), // TODO remove; no rel. DB
                "podcastId"             -> toBson(f.getPodcastId), // TODO remove; no rel. DB
                "exo"                   -> toBson(f.getExo),
                "podcastExo"            -> toBson(f.getPodcastExo),
                "url"                   -> toBson(f.getUrl),
                "lastChecked"           -> toBson(f.getLastChecked),
                "lastStatus"            -> toBson(f.getLastStatus.getName),
                "registrationTimestamp" -> toBson(f.getRegistrationTimestamp)
            )
            toBson(map)
        }
    }

    implicit object ChapterReader extends BSONDocumentReader[Chapter] {
        override def read(bson: BSONDocument): Chapter = {
            implicit val implicitBson: BSONDocument = bson
            val c = ImmutableChapter.builder()

            asLong("id").map(c.setId(_))
            asLong("episodeId").map(c.setEpisodeId(_))
            asString("start").map(c.setStart)
            asString("title").map(c.setTitle)
            asString("href").map(c.setHref)
            asString("image").map(c.setImage)
            asString("episodeExo").map(c.setEpisodeExo)

            c.create()

            /*
            val opt: Option[Chapter] = for {
                id         <- asLong("id") // TODO remove; no rel. DB
                episodeId  <- asLong("episodeId") // TODO remove; no rel. DB
                start      <- asString("start")
                title      <- asString("title")
                href       <- asString("href")
                image      <- asString("image")
                episodeExo <- asString("episodeExo")
            } yield builder
                .setId(id)
                .setEpisodeId(episodeId)
                .setStart(start)
                .setTitle(title)
                .setHref(href)
                .setImage(image)
                .setEpisodeExo(episodeExo)
                .create()

            opt.get // the Chapter is required (or let throw an exception)
            */
        }
    }

    implicit object ChapterWriter extends BSONDocumentWriter[Chapter] {
        override def write(c: Chapter): BSONDocument = {
            val map: Map[String, Option[BSONValue]] = Map(
                "id" -> toBson(c.getId), // TODO remove; no rel. DB
                "episodeId"  -> toBson(c.getEpisodeId), // TODO remove; no rel. DB
                "start"      -> toBson(c.getStart),
                "title"      -> toBson(c.getTitle),
                "href"       -> toBson(c.getHref),
                "image"      -> toBson(c.getImage),
                "episodeExo" -> toBson(c.getEpisodeExo)
            )
            toBson(map)
        }

    }

}
