package io.disposia.engine.domain.episode

import io.disposia.engine.catalog.repository.BsonConversion
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

case class EpisodeItunesInfo(
                               duration: Option[String] = None,
                               subtitle: Option[String] = None,
                               author: Option[String] = None,
                               summary: Option[String] = None,
                               season: Option[Int] = None,
                               episode: Option[Int] = None,
                               episodeType: Option[String] = None
                             )
