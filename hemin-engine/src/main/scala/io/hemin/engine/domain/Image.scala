package io.hemin.engine.domain

import java.time.LocalDateTime

final case class Image (
  id: Option[String]               = None,
  associateId: Option[String]      = None,
  data: Option[Array[Byte]]        = None,
  hash: Option[String]             = None,
  name: Option[String]             = None,
  contentType: Option[String]      = None,
  size: Option[Long]               = None,
  createdAt: Option[LocalDateTime] = None,
) extends Patchable[Image] {

  override def patchLeft(diff: Image): Image = Option(diff) match {
    case None => this
    case Some(that) => Image(
      id          = reduceLeft(this.id, that.id),
      associateId = reduceLeft(this.associateId, that.associateId),
      data        = reduceLeft(this.data, that.data),
      hash        = reduceLeft(this.hash, that.hash),
      name        = reduceLeft(this.name, that.name),
      contentType = reduceLeft(this.contentType, that.contentType),
      size        = reduceLeft(this.size, that.size),
      createdAt   = reduceLeft(this.createdAt, that.createdAt),
    )
  }

  override def patchRight(diff: Image): Image = Option(diff) match {
    case None => this
    case Some(that) => Image(
      id          = reduceRight(this.id, that.id),
      associateId = reduceRight(this.associateId, that.associateId),
      data        = reduceRight(this.data, that.data),
      hash        = reduceRight(this.hash, that.hash),
      name        = reduceRight(this.name, that.name),
      contentType = reduceRight(this.contentType, that.contentType),
      size        = reduceRight(this.size, that.size),
      createdAt   = reduceRight(this.createdAt, that.createdAt),
    )
  }
}
