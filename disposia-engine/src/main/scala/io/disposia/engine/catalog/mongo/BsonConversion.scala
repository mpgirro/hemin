package io.disposia.engine.catalog.mongo

import java.time.LocalDateTime

import com.google.common.collect.Sets
import io.disposia.engine.domain.FeedStatus
import io.disposia.engine.domain.dto._
import io.disposia.engine.mapper.DateMapper
import reactivemongo.bson.{BSONBoolean, BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONInteger, BSONLong, BSONNumberLike}

import scala.collection.JavaConverters._


object BsonConversion {

    def toBson(b: Boolean): BSONBoolean = BSONBoolean(b)
    def toBson(i: Int): BSONInteger = BSONInteger(i)
    def toBson(l: Long): BSONLong = BSONLong(l)
    def toBson(d: LocalDateTime): BSONDateTime = BSONDateTime(DateMapper.INSTANCE.asMilliseconds(d))

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
            val builder = ImmutablePodcast.builder()
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
                itunesExplicit        <- bson.getAs[Boolean]("itunesExplicit")
                itunesBlock           <- bson.getAs[Boolean]("itunesBlock")
                itunesType            <- asString("itunesType")
                itunesOwnerName       <- asString("itunesOwnerName")
                itunesOwnerEmail      <- asString("itunesOwnerEmail")
                feedpressLocale       <- asString("feedpressLocale")
                fyydVerify            <- asString("fyydVerify")
                episodeCount          <- asInt("episodeCount")
                registrationTimestamp <- asLocalDateTime("registrationTimestamp")
                registrationComplete  <- asBoolean("registrationComplete")
            } yield builder
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
        }
    }

    implicit object PodcastWriter extends BSONDocumentWriter[Podcast] {
        override def write(p: Podcast): BSONDocument =
            BSONDocument(
                "id"                    -> toBson(p.getId), // TODO remove; no rel. DB
                "exo"                   -> p.getExo,
                "title"                 -> p.getTitle,
                "link"                  -> p.getLink,
                "description"           -> p.getDescription,
                "pubDate"               -> toBson(p.getPubDate),
                "lastBuildDate"         -> toBson(p.getLastBuildDate),
                "language"              -> p.getLanguage,
                "generator"             -> p.getGenerator,
                "copyright"             -> p.getCopyright,
                "docs"                  -> p.getDocs,
                "managingEditor"        -> p.getManagingEditor,
                "image"                 -> p.getImage,
                "itunesSummary"         -> p.getItunesSummary,
                "itunesAuthor"          -> p.getItunesAuthor,
                "itunesKeywords"        -> p.getItunesKeywords,
                "itunesCategorie"       -> p.getItunesCategories.asScala.mkString("|"), // TODO collection!
                "itunesExplicit"        -> toBson(p.getItunesExplicit),
                "itunesBlock"           -> toBson(p.getItunesBlock),
                "itunesType"            -> p.getItunesType,
                "itunesOwnerName"       -> p.getItunesOwnerName,
                "itunesOwnerEmail"      -> p.getItunesOwnerEmail,
                "feedpressLocale"       -> p.getFeedpressLocale,
                "fyydVerify"            -> p.getFyydVerify,
                "episodeCount"          -> toBson(p.getEpisodeCount), // TODO will ich das feld hier ausbauen?
                "registrationTimestamp" -> toBson(p.getRegistrationTimestamp),
                "registrationComplete"  -> toBson(p.getRegistrationComplete)
            )
    }

    implicit object EpisodeReader extends BSONDocumentReader[Episode] {
        override def read(bson: BSONDocument): Episode = {
            implicit val implicitBson: BSONDocument = bson
            val builder = ImmutableEpisode.builder()
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
                guidIsPermalink       <- bson.getAs[Boolean]("guidIsPermalink")
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
            } yield builder
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
        }
    }

    implicit object EpisodeWriter extends BSONDocumentWriter[Episode] {
        override def write(e: Episode): BSONDocument =
            BSONDocument(
                "id"                    -> toBson(e.getId), // TODO remove; no rel. DB
                "podcastId"             -> toBson(e.getPodcastId), // TODO remove; no rel. DB
                "exo"                   -> e.getExo,
                "podcastExo"            -> e.getPodcastExo,
                "podcastTitle"          -> e.getPodcastTitle,
                "title"                 -> e.getTitle,
                "link"                  -> e.getLink,
                "pubDate"               -> toBson(e.getPubDate),
                "guid"                  -> e.getGuid,
                "guidIsPermalink"       -> toBson(e.getGuidIsPermaLink),
                "description"           -> e.getDescription,
                "image"                 -> e.getImage,
                "itunesDuration"        -> e.getItunesDuration,
                "itunesSubtitle"        -> e.getItunesSubtitle,
                "itunesAuthor"          -> e.getItunesAuthor,
                "itunesSummary"         -> e.getItunesSummary,
                "itunesSeason"          -> toBson(e.getItunesSeason),
                "itunesEpisode"         -> toBson(e.getItunesEpisode),
                "itunesEpisodeType"     -> e.getItunesEpisodeType,
                "enclosureUrl"          -> e.getEnclosureUrl,
                "enclosureLength"       -> toBson(e.getEnclosureLength),
                "enclosureType"         -> e.getEnclosureType,
                "contentEncoded"        -> e.getContentEncoded,
                "registrationTimestamp" -> toBson(e.getRegistrationTimestamp),
            )
    }

    implicit object FeedReader extends BSONDocumentReader[Feed] {
        override def read(bson: BSONDocument): Feed = {
            implicit val implicitBson: BSONDocument = bson
            val builder = ImmutableFeed.builder()
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
        }
    }

    implicit object FeedWriter extends BSONDocumentWriter[Feed] {
        override def write(f: Feed): BSONDocument =
            BSONDocument(
                "id"                    -> toBson(f.getId), // TODO remove; no rel. DB
                "podcastId"             -> toBson(f.getPodcastId), // TODO remove; no rel. DB
                "exo"                   -> f.getExo,
                "podcastExo"            -> f.getPodcastExo,
                "url"                   -> f.getUrl,
                "lastChecked"           -> toBson(f.getLastChecked),
                "lastStatus"            -> f.getLastStatus.getName,
                "registrationTimestamp" -> toBson(f.getRegistrationTimestamp)
            )
    }

    implicit object ChapterReader extends BSONDocumentReader[Chapter] {
        override def read(bson: BSONDocument): Chapter = {
            implicit val implicitBson: BSONDocument = bson
            val builder = ImmutableChapter.builder()
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
        }
    }

    implicit object ChapterWriter extends BSONDocumentWriter[Chapter] {
        override def write(c: Chapter): BSONDocument =
            BSONDocument(
                "id" -> toBson(c.getId), // TODO remove; no rel. DB
                "episodeId"  -> toBson(c.getEpisodeId), // TODO remove; no rel. DB
                "start"      -> c.getStart,
                "title"      -> c.getTitle,
                "href"       -> c.getHref,
                "image"      -> c.getImage,
                "episodeExo" -> c.getEpisodeExo
            )
    }

}
