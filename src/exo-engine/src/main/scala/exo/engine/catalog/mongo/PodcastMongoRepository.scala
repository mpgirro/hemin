package exo.engine.catalog.mongo

import com.google.common.collect.Sets
import exo.engine.domain.dto.{ImmutablePodcast, Podcast}
import exo.engine.mapper.DateMapper
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONBoolean, BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONInteger, BSONLong, BSONNumberLike}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author max
  */
class PodcastMongoRepository (db: DefaultDB)
                             (implicit ec: ExecutionContext) {

    //private def collection: BSONCollection = db.collection("podcasts")
    private def collection: BSONCollection = db.collection("podcasts")

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
                            itunesCategories
                                .split("|"))))
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

            opt.get // the person is required (or let throw an exception)
        }
    }

    private implicit object EpisodeWriter extends BSONDocumentWriter[Podcast] {
        override def write(podcast: Podcast): BSONDocument =
            BSONDocument(
                "id" -> BSONLong(podcast.getId), // TODO remove; no rel. DB
                "exo" -> podcast.getExo,
                "title"-> podcast.getTitle,
                "link" -> podcast.getLink,
                "description" -> podcast.getDescription,
                "pubDate" -> BSONDateTime(DateMapper.INSTANCE.asMilliseconds(podcast.getPubDate)),
                "lastBuildDate" -> BSONDateTime(DateMapper.INSTANCE.asMilliseconds(podcast.getLastBuildDate)),
                "language" -> podcast.getLanguage,
                "generator" -> podcast.getGenerator,
                "copyright" -> podcast.getCopyright,
                "docs" -> podcast.getDocs,
                "managingEditor" -> podcast.getManagingEditor,
                "image" -> podcast.getImage,
                "itunesSummary" -> podcast.getItunesSummary,
                "itunesAuthor" -> podcast.getItunesAuthor,
                "itunesKeywords" -> podcast.getItunesKeywords,
                "itunesCategorie" -> podcast.getItunesCategories.asScala.mkString("|"), // TODO collection!
                "itunesExplicit" -> BSONBoolean(podcast.getItunesExplicit),
                "itunesBlock" -> BSONBoolean(podcast.getItunesBlock),
                "itunesType" -> podcast.getItunesType,
                "itunesOwnerName" -> podcast.getItunesOwnerName,
                "itunesOwnerEmail" -> podcast.getItunesOwnerEmail,
                "feedpressLocale" -> podcast.getFeedpressLocale,
                "fyydVerify" -> podcast.getFyydVerify,
                "episodeCount" -> BSONInteger(podcast.getEpisodeCount), // TODO will ich das feld hier ausbauen?
                "registrationTimestamp" -> BSONDateTime(DateMapper.INSTANCE.asMilliseconds(podcast.getRegistrationTimestamp)),
                "registrationComplete" -> BSONBoolean(podcast.getRegistrationComplete)
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
