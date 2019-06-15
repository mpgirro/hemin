package io.hemin.engine.model

import com.rometools.rome.feed.synd.SyndPerson

object Person {

  /** Instantiates a [[io.hemin.engine.model.Person]] from a ROME SyndPerson object */
  def fromRome(p: SyndPerson): Person = Person(
    name  = Option(p.getName),
    email = Option(p.getEmail),
    uri   = Option(p.getUri),
  )

}

final case class Person(
  name: Option[String]  = None,
  email: Option[String] = None,
  uri: Option[String]   = None,
) extends Patchable[Person]
  with Documentable {

  override def documentType: DocumentType = DocumentType.Person

  override def patchLeft(diff: Person): Person = Option(diff) match {
    case None       => this
    case Some(that) => Person(
      name  = reduceLeft(this.name, that.name),
      email = reduceLeft(this.email, that.email),
      uri   = reduceLeft(this.uri, that.uri),
    )
  }

  override def patchRight(diff: Person): Person = Option(diff) match {
    case None       => this
    case Some(that) => Person(
      name  = reduceRight(this.name, that.name),
      email = reduceRight(this.email, that.email),
      uri   = reduceRight(this.uri, that.uri),
    )
  }

}
