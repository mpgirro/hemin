package io.hemin.engine.model

import reactivemongo.bson.Macros.Annotations.Key

final case class Episode(
  @Key("_id")
  id: Option[String]                = None,
  podcastId: Option[String]         = None,
  podcastTitle: Option[String]      = None,
  title: Option[String]             = None,
  link: Option[String]              = None,
  pubDate: Option[Long]             = None,
  guid: Option[String]              = None,
  guidIsPermalink: Option[Boolean]  = None,
  description: Option[String]       = None,
  image: Option[String]             = None,
  contentEncoded: Option[String]    = None,
  chapters: List[Chapter]           = Nil,
  atom: Atom                        = Atom(),
  persona: Persona                  = Persona(),
  itunes: EpisodeItunes             = EpisodeItunes(),
  enclosure: EpisodeEnclosure       = EpisodeEnclosure(),
  registration: EpisodeRegistration = EpisodeRegistration(),
) extends Patchable[Episode]
  with Documentable {

  override def documentType: DocumentType = DocumentType.Episode

  override def patchLeft(diff: Episode): Episode = Option(diff) match {
    case None       => this
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
      chapters        = reduceLeft(this.chapters, that.chapters),
      atom            = this.atom.patchLeft(that.atom),
      persona         = this.persona.patchLeft(that.persona),
      itunes          = this.itunes.patchLeft(that.itunes),
      enclosure       = this.enclosure.patchLeft(that.enclosure),
      registration    = this.registration.patchLeft(that.registration),
    )
  }

  override def patchRight(diff: Episode): Episode = Option(diff) match {
    case None       => this
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
      chapters        = reduceLeft(this.chapters, that.chapters),
      atom            = this.atom.patchRight(that.atom),
      persona         = this.persona.patchRight(that.persona),
      itunes          = this.itunes.patchRight(that.itunes),
      enclosure       = this.enclosure.patchRight(that.enclosure),
      registration    = this.registration.patchRight(that.registration),
    )
  }
}
