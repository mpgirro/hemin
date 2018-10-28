package io.hemin.engine.model

import enumeratum._

import scala.collection.immutable

sealed abstract class IndexField(override val entryName: String) extends EnumEntry

object IndexField
  extends Enum[IndexField] {

  /* `findValues` is a protected method that invokes a macro to
   * find all `Greeting` object declarations inside an `Enum`.
   * We use it to implement the `val values` member */
  val values: immutable.IndexedSeq[IndexField] = findValues

  case object DocType        extends IndexField("doc_type")
  case object Id             extends IndexField("id")
  case object Title          extends IndexField("title")
  case object Description    extends IndexField("description")
  case object Link           extends IndexField("link")
  case object PubDate        extends IndexField("pub_date")
  case object PodcastTitle   extends IndexField("podcast_title")
  case object ItunesImage    extends IndexField("itunes_image")
  case object ItunesDuration extends IndexField("itunes_duration")
  case object ItunesAuthor   extends IndexField("itunes_author")
  case object ItunesSummary  extends IndexField("itunes_summary")
  case object ChapterMarks   extends IndexField("chapter_marks")
  case object ContentEncoded extends IndexField("content_encoded")
  case object Transcript     extends IndexField("transcript")
  case object WebsiteData    extends IndexField("website_data")

}
