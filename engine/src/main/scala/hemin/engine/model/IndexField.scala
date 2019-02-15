package hemin.engine.model

import enumeratum._

import scala.collection.immutable

sealed abstract class IndexField(override val entryName: String) extends EnumEntry

object IndexField
  extends Enum[IndexField] {

  /* `findValues` is a protected method that invokes a macro to
   * find all `IndexField` object declarations inside an `Enum`.
   * We use it to implement the `val values` member */
  val values: immutable.IndexedSeq[IndexField] = findValues

  final case object DocType        extends IndexField("doc_type")
  final case object Id             extends IndexField("id")
  final case object Title          extends IndexField("title")
  final case object Description    extends IndexField("description")
  final case object Link           extends IndexField("link")
  final case object LinkKeywords   extends IndexField("link_keywords")
  final case object PubDate        extends IndexField("pub_date")
  final case object PodcastTitle   extends IndexField("podcast_title")
  final case object ItunesImage    extends IndexField("itunes_image")
  final case object ItunesDuration extends IndexField("itunes_duration")
  final case object ItunesAuthor   extends IndexField("itunes_author")
  final case object ItunesSummary  extends IndexField("itunes_summary")
  final case object ChapterMarks   extends IndexField("chapter_marks")
  final case object ContentEncoded extends IndexField("content_encoded")
  final case object Transcript     extends IndexField("transcript")
  final case object WebsiteData    extends IndexField("website_data")

}
