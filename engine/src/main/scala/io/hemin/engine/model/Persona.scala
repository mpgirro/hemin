package io.hemin.engine.model

final case class Persona(
  authors: List[Person]      = Nil,
  contributors: List[Person] = Nil,
) extends Patchable[Persona] {

  override def patchLeft(diff: Persona): Persona = Option(diff) match {
    case None       => this
    case Some(that) => Persona(
      authors      = reduceLeft(this.authors, that.authors),
      contributors = reduceLeft(this.contributors, that.contributors),
    )
  }

  override def patchRight(diff: Persona): Persona = Option(diff) match {
    case None       => this
    case Some(that) => Persona(
      authors      = reduceRight(this.authors, that.authors),
      contributors = reduceRight(this.contributors, that.contributors),
    )
  }

}
