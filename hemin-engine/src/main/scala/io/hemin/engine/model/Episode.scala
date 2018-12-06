package io.hemin.engine.model

import java.time.LocalDateTime

final case class Episode(
                          id: Option[String]                    = None,
                          podcastId: Option[String]             = None,
                          podcastTitle: Option[String]          = None,
                          title: Option[String]                 = None,
                          link: Option[String]                  = None,
                          pubDate: Option[LocalDateTime]        = None,
                          guid: Option[String]                  = None,
                          guidIsPermalink: Option[Boolean]      = None,
                          description: Option[String]           = None,
                          image: Option[String]                 = None,
                          contentEncoded: Option[String]        = None,
                          atomLinks: List[AtomLink]             = Nil,
                          chapters: List[Chapter]               = Nil,
                          itunes: EpisodeItunes             = EpisodeItunes(),
                          enclosure: EpisodeEnclosure       = EpisodeEnclosure(),
                          registration: EpisodeRegistration = EpisodeRegistration(),
) extends Patchable[Episode] {

  override def patchLeft(diff: Episode): Episode = Option(diff) match {
    case None => this
    case Some(that) => Episode(
      id              = reduceLeft(this.id, that.id),
      podcastId       = reduceLeft(this.podcastId, that.podcastId),
      podcastTitle    = reduceLeft(this.podcastTitle, that.podcastTitle),
      title           = reduceLeft(this.title, that.title),
      link            = reduceLeft(this.link, that.link),
      pubDate         = reduceLeft(this.pubDate, that.pubDate),
      guid            = reduceLeft(this.guid, that.guid),
      guidIsPermalink = reduceLeft(this.guidIsPermalink, that.guidIsPermalink),
      description     = reduceLeft(this.description, that.description),
      image           = reduceLeft(this.image, that.image),
      contentEncoded  = reduceLeft(this.contentEncoded, that.contentEncoded),
      atomLinks       = reduceLeft(this.atomLinks, that.atomLinks),
      chapters        = reduceLeft(this.chapters, that.chapters),
      itunes = EpisodeItunes(
        duration    = reduceLeft(this.itunes.duration, that.itunes.duration),
        subtitle    = reduceLeft(this.itunes.subtitle, that.itunes.subtitle),
        author      = reduceLeft(this.itunes.author, that.itunes.author),
        summary     = reduceLeft(this.itunes.summary, that.itunes.summary),
        season      = reduceLeft(this.itunes.season, that.itunes.season),
        episode     = reduceLeft(this.itunes.episode, that.itunes.episode),
        episodeType = reduceLeft(this.itunes.episodeType, that.itunes.episodeType),
      ),
      enclosure = EpisodeEnclosure(
        url    = reduceLeft(this.enclosure.url, that.enclosure.url),
        length = reduceLeft(this.enclosure.length, that.enclosure.length),
        typ    = reduceLeft(this.enclosure.typ, that.enclosure.typ),
      ),
      registration = EpisodeRegistration(
        timestamp = reduceLeft(this.registration.timestamp, that.registration.timestamp)
      )
    )
  }

  override def patchRight(diff: Episode): Episode = Option(diff) match {
    case None => this
    case Some(that) => Episode(
      id              = reduceRight(this.id, that.id),
      podcastId       = reduceRight(this.podcastId, that.podcastId),
      podcastTitle    = reduceRight(this.podcastTitle, that.podcastTitle),
      title           = reduceRight(this.title, that.title),
      link            = reduceRight(this.link, that.link),
      pubDate         = reduceRight(this.pubDate, that.pubDate),
      guid            = reduceRight(this.guid, that.guid),
      guidIsPermalink = reduceRight(this.guidIsPermalink, that.guidIsPermalink),
      description     = reduceRight(this.description, that.description),
      image           = reduceRight(this.image, that.image),
      contentEncoded  = reduceRight(this.contentEncoded, that.contentEncoded),
      atomLinks       = reduceRight(this.atomLinks, that.atomLinks),
      chapters        = reduceLeft(this.chapters, that.chapters),
      itunes = EpisodeItunes(
        duration    = reduceRight(this.itunes.duration, that.itunes.duration),
        subtitle    = reduceRight(this.itunes.subtitle, that.itunes.subtitle),
        author      = reduceRight(this.itunes.author, that.itunes.author),
        summary     = reduceRight(this.itunes.summary, that.itunes.summary),
        season      = reduceRight(this.itunes.season, that.itunes.season),
        episode     = reduceRight(this.itunes.episode, that.itunes.episode),
        episodeType = reduceRight(this.itunes.episodeType, that.itunes.episodeType),
      ),
      enclosure = EpisodeEnclosure(
        url    = reduceRight(this.enclosure.url, that.enclosure.url),
        length = reduceRight(this.enclosure.length, that.enclosure.length),
        typ    = reduceRight(this.enclosure.typ, that.enclosure.typ),
      ),
      registration = EpisodeRegistration(
        timestamp = reduceRight(this.registration.timestamp, that.registration.timestamp)
      )
    )
  }
}
