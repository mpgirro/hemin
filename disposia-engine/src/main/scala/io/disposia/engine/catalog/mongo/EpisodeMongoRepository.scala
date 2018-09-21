package io.disposia.engine.catalog.mongo

import io.disposia.engine.catalog.mongo.BsonWrites.toBson
import io.disposia.engine.domain.dto.{Episode, ImmutableEpisode}
import io.disposia.engine.mapper.DateMapper
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author max
  */
class EpisodeMongoRepository (db: DefaultDB)
                             (implicit ec: ExecutionContext) {

    private def collection: BSONCollection = db.collection("episodes")
    collection.create() // TODO ensure that the collection exists -> brauch ich das? mache ich nur weil Podcasts/Chapters gerade nicht geschrieben werden

    private implicit object EpisodeReader extends BSONDocumentReader[Episode] {
        override def read(bson: BSONDocument): Episode = {
            val builder = ImmutableEpisode.builder()
            val opt: Option[Episode] = for {
                id <- bson.getAs[BSONNumberLike]("id").map(_.toLong) // TODO remove; no rel. DB
                podcastId <- bson.getAs[BSONNumberLike]("podcastId").map(_.toLong) // TODO remove; no rel. DB
                exo <- bson.getAs[String]("exo")
                podcastExo <- bson.getAs[String]("podcastExo")
                podcastTitle <- bson.getAs[String]("podcastTitle")
                title <- bson.getAs[String]("title")
                link <- bson.getAs[String]("link")
                pubDate <- bson.getAs[BSONDateTime]("pubDate").map(dt => DateMapper.INSTANCE.asLocalDateTime(dt.value))
                guid <- bson.getAs[String]("guid")
                guidIsPermalink <- bson.getAs[Boolean]("guidIsPermalink")
                description <- bson.getAs[String]("description")
                image <- bson.getAs[String]("image")
                itunesDuration <- bson.getAs[String]("itunesDuration")
                itunesSubtitle <- bson.getAs[String]("itunesSubtitle")
                itunesAuthor <- bson.getAs[String]("itunesAuthor")
                itunesSummary <- bson.getAs[String]("itunesSummary")
                itunesSeason <- bson.getAs[BSONNumberLike]("itunesSeason").map(_.toInt)
                itunesEpisode <- bson.getAs[BSONNumberLike]("itunesEpisode").map(_.toInt)
                itunesEpisodeType <- bson.getAs[String]("itunesEpisodeType")
                enclosureUrl <- bson.getAs[String]("enclosureUrl")
                enclosureLength <- bson.getAs[BSONNumberLike]("enclosureLength").map(_.toLong)
                enclosureType <- bson.getAs[String]("enclosureType")
                contentEncoded <- bson.getAs[String]("contentEncoded")
                registrationTimestamp <- bson.getAs[BSONDateTime]("registrationTimestamp").map(dt => DateMapper.INSTANCE.asLocalDateTime(dt.value))
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

    private implicit object EpisodeWriter extends BSONDocumentWriter[Episode] {
        override def write(e: Episode): BSONDocument =
            BSONDocument(
                "id" -> toBson(e.getId), // TODO remove; no rel. DB
                "podcastId" -> toBson(e.getPodcastId), // TODO remove; no rel. DB
                "exo" -> e.getExo,
                "podcastExo" -> e.getPodcastExo,
                "podcastTitle" -> e.getPodcastTitle,
                "title"-> e.getTitle,
                "link" -> e.getLink,
                "pubDate" -> toBson(e.getPubDate),
                "guid" -> e.getGuid,
                "guidIsPermalink" -> toBson(e.getGuidIsPermaLink),
                "description" -> e.getDescription,
                "image" -> e.getImage,
                "itunesDuration" -> e.getItunesDuration,
                "itunesSubtitle" -> e.getItunesSubtitle,
                "itunesAuthor" -> e.getItunesAuthor,
                "itunesSummary" -> e.getItunesSummary,
                "itunesSeason" -> toBson(e.getItunesSeason),
                "itunesEpisode" -> toBson(e.getItunesEpisode),
                "itunesEpisodeType" -> e.getItunesEpisodeType,
                "enclosureUrl" -> e.getEnclosureUrl,
                "enclosureLength" -> toBson(e.getEnclosureLength),
                "enclosureType" -> e.getEnclosureType,
                "contentEncoded" -> e.getContentEncoded,
                "registrationTimestamp" -> toBson(e.getRegistrationTimestamp),
            )
    }

    def save(episode: Episode): Future[Unit] = {
        collection
            .insert[Episode](ordered = false)
            .one(episode)
            .map(_ => {})
    }

    def findByExo(exo: String): Future[Option[Episode]] = {
        val query = BSONDocument("exo" -> exo)
        collection
            .find(query)
            .one[Episode]
    }

}
