package io.hemin.engine.model

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

sealed abstract class FeedStatus(override val entryName: String) extends EnumEntry

object FeedStatus extends Enum[FeedStatus] {

  /* `findValues` is a protected method that invokes a macro to
 * find all `FeedStatus` object declarations inside an `Enum`.
 * We use it to implement the `val values` member */
  val values: immutable.IndexedSeq[FeedStatus] = findValues

  final case object NeverChecked    extends FeedStatus("never_checked")
  final case object DownloadSuccess extends FeedStatus("download_success")
  final case object DownloadError   extends FeedStatus("download_error")
  final case object Http403         extends FeedStatus("http_403")
  final case object ParserError     extends FeedStatus("parser_error")

}
