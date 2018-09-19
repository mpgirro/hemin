package exo.engine.catalog.mongo

import java.time.LocalDateTime

import exo.engine.domain.dto.{Chapter, Episode, ImmutableEpisode}
import exo.engine.mapper.DateMapper
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONBoolean, BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONInteger, BSONLong, BSONNumberLike}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author max
  */
class EpisodeMongoRepository (db: DefaultDB)
                             (implicit ec: ExecutionContext) {

    //private def collection: BSONCollection = db.collection("episodes")
    private def collection: BSONCollection = db.collection("episodes")

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

            opt.get // the person is required (or let throw an exception)
        }
    }

    private implicit object EpisodeWriter extends BSONDocumentWriter[Episode] {
        override def write(episode: Episode): BSONDocument =
            BSONDocument(
                "id" -> BSONLong(episode.getId), // TODO remove; no rel. DB
                "podcastId" -> BSONLong(episode.getPodcastId), // TODO remove; no rel. DB
                "exo" -> episode.getExo,
                "podcastExo" -> episode.getPodcastExo,
                "podcastTitle" -> episode.getPodcastTitle,
                "title"-> episode.getTitle,
                "link" -> episode.getLink,
                "pubDate" -> BSONDateTime(DateMapper.INSTANCE.asMilliseconds(episode.getPubDate)),
                "guid" -> episode.getGuid,
                "guidIsPermalink" -> BSONBoolean(episode.getGuidIsPermaLink),
                "description" -> episode.getDescription,
                "image" -> episode.getImage,
                "itunesDuration" -> episode.getItunesDuration,
                "itunesSubtitle" -> episode.getItunesSubtitle,
                "itunesAuthor" -> episode.getItunesAuthor,
                "itunesSummary" -> episode.getItunesSummary,
                "itunesSeason" -> BSONInteger(episode.getItunesSeason),
                "itunesEpisode" -> BSONInteger(episode.getItunesEpisode),
                "itunesEpisodeType" -> episode.getItunesEpisodeType,
                "enclosureUrl" -> episode.getEnclosureUrl,
                "enclosureLength" -> BSONLong(episode.getEnclosureLength),
                "enclosureType" -> episode.getEnclosureType,
                "contentEncoded" -> episode.getContentEncoded,
                "registrationTimestamp" -> BSONDateTime(DateMapper.INSTANCE.asMilliseconds(episode.getRegistrationTimestamp)),
            )
    }

    def save(episode: Episode): Future[Unit] = {

        //println("Saving [MongoEpisodeService] : " + episode.toString)
        /*
        collection
            .insert[Episode](ordered = false)
            .one(episode)
            .map(_ => {})
            */

        collection
            .insert[Episode](ordered = false)
            .one(episode)
            .map(_ => {})
    }

    def findByExo(exo: String): Future[Option[Episode]] = {
        val query = BSONDocument("exo" -> exo)
        /*
        collection
            .find(query)
            .one[Episode]
            */
        collection
            .find(query)
            .one[Episode]
    }

}
