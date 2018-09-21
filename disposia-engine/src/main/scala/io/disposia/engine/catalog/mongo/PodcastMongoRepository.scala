package exo.engine.catalog.mongo

import com.google.common.collect.Sets
import io.disposia.engine.domain.dto.{ImmutablePodcast, Podcast}
import io.disposia.engine.mapper.DateMapper
import exo.engine.catalog.mongo.BsonWrites.toBson
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class PodcastMongoRepository (db: DefaultDB)
                             (implicit ec: ExecutionContext) {

    private def collection: BSONCollection = db.collection("podcasts")
    collection.create() // TODO ensure that the collection exists -> brauch ich das? mache ich nur weil Podcasts/Chapters gerade nicht geschrieben werden

    private implicit object EpisodeReader extends BSONDocumentReader[Podcast] {
        override def read(bson: BSONDocument): Podcast = {
            val builder = ImmutablePodcast.builder()
            val opt: Option[Podcast] = for {
                id <- bson.getAs[BSONNumberLike]("id").map(_.toLong) // TODO remove; no rel. DB
                exo <- bson.getAs[String]("exo")
                title <- bson.getAs[String]("title")
                link <- bson.getAs[String]("link")
                description <- bson.getAs[String]("description")
                pubDate <- bson.getAs[BSONDateTime]("pubDate").map(dt => DateMapper.INSTANCE.asLocalDateTime(dt.value))
                lastBuildDate <- bson.getAs[BSONDateTime]("lastBuildDate").map(dt => DateMapper.INSTANCE.asLocalDateTime(dt.value))
                language <- bson.getAs[String]("language")
                generator <- bson.getAs[String]("generator")
                copyright <- bson.getAs[String]("copyright")
                docs <- bson.getAs[String]("docs")
                managingEditor <- bson.getAs[String]("managingEditor")
                image <- bson.getAs[String]("image")
                itunesSummary <- bson.getAs[String]("itunesSummary")
                itunesAuthor <- bson.getAs[String]("itunesAuthor")
                itunesKeywords <- bson.getAs[String]("itunesKeywords")
                itunesCategories <- bson.getAs[String]("itunesCategories")
                itunesExplicit <- bson.getAs[Boolean]("itunesExplicit")
                itunesBlock <- bson.getAs[Boolean]("itunesBlock")
                itunesType <- bson.getAs[String]("itunesType")
                itunesOwnerName <- bson.getAs[String]("itunesOwnerName")
                itunesOwnerEmail <- bson.getAs[String]("itunesOwnerEmail")
                feedpressLocale <- bson.getAs[String]("feedpressLocale")
                fyydVerify <- bson.getAs[String]("fyydVerify")
                episodeCount <- bson.getAs[BSONNumberLike]("episodeCount").map(_.toInt)
                registrationTimestamp <- bson.getAs[BSONDateTime]("registrationTimestamp").map(dt => DateMapper.INSTANCE.asLocalDateTime(dt.value))
                registrationComplete <- bson.getAs[Boolean]("registrationComplete")
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

    private implicit object EpisodeWriter extends BSONDocumentWriter[Podcast] {
        override def write(p: Podcast): BSONDocument =
            BSONDocument(
                "id" -> toBson(p.getId), // TODO remove; no rel. DB
                "exo" -> p.getExo,
                "title"-> p.getTitle,
                "link" -> p.getLink,
                "description" -> p.getDescription,
                "pubDate" -> toBson(p.getPubDate),
                "lastBuildDate" -> toBson(p.getLastBuildDate),
                "language" -> p.getLanguage,
                "generator" -> p.getGenerator,
                "copyright" -> p.getCopyright,
                "docs" -> p.getDocs,
                "managingEditor" -> p.getManagingEditor,
                "image" -> p.getImage,
                "itunesSummary" -> p.getItunesSummary,
                "itunesAuthor" -> p.getItunesAuthor,
                "itunesKeywords" -> p.getItunesKeywords,
                "itunesCategorie" -> p.getItunesCategories.asScala.mkString("|"), // TODO collection!
                "itunesExplicit" -> toBson(p.getItunesExplicit),
                "itunesBlock" -> toBson(p.getItunesBlock),
                "itunesType" -> p.getItunesType,
                "itunesOwnerName" -> p.getItunesOwnerName,
                "itunesOwnerEmail" -> p.getItunesOwnerEmail,
                "feedpressLocale" -> p.getFeedpressLocale,
                "fyydVerify" -> p.getFyydVerify,
                "episodeCount" -> toBson(p.getEpisodeCount), // TODO will ich das feld hier ausbauen?
                "registrationTimestamp" -> toBson(p.getRegistrationTimestamp),
                "registrationComplete" -> toBson(p.getRegistrationComplete)
            )
    }

    def save(podcast: Podcast): Future[Unit] = {
        collection
            .insert[Podcast](ordered = false)
            .one(podcast)
            .map(_ => {})
    }

    def findByExo(exo: String): Future[Option[Podcast]] = {
        val query = BSONDocument("exo" -> exo)
        collection
            .find(query)
            .one[Podcast]
    }

}
