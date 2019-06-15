package io.hemin.engine.model

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed abstract class DocumentType(override val entryName: String) extends EnumEntry

object DocumentType extends Enum[DocumentType] {

  /**
   * `findValues` is a protected method that invokes a macro to
   * find all `DocumentType` object declarations inside an `Enum`.
   * We use it to implement the `val values` member
   */
  val values: immutable.IndexedSeq[DocumentType] = findValues

  final case object Podcast extends DocumentType("podcast")
  final case object Episode extends DocumentType("episode")
  final case object Person  extends DocumentType("person")

}
