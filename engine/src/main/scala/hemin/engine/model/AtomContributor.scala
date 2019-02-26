package hemin.engine.model

final case class AtomContributor(
  name: Option[String]  = None,
  email: Option[String] = None,
  uri: Option[String]   = None,
) extends Patchable[AtomContributor] {

  override def patchLeft(diff: AtomContributor): AtomContributor = Option(diff) match {
    case None       => this
    case Some(that) => AtomContributor(
      name  = reduceLeft(this.name, that.name),
      email = reduceLeft(this.email, that.email),
      uri   = reduceLeft(this.uri, that.uri),
    )
  }

  override def patchRight(diff: AtomContributor): AtomContributor = Option(diff) match {
    case None       => this
    case Some(that) => AtomContributor(
      name  = reduceRight(this.name, that.name),
      email = reduceRight(this.email, that.email),
      uri   = reduceRight(this.uri, that.uri),
    )
  }

}
