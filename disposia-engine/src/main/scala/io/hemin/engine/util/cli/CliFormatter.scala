package io.hemin.engine.util.cli

import io.hemin.engine.domain._

object CliFormatter {

  /* TODO 2018-10-08: once PPrint support outputting field names (see https://github.com/lihaoyi/PPrint/issues/4) , I want to use these implementations
   *
  def format(podcast: Podcast): String = pprint.apply(podcast).toString()

  def format(episode: Episode): String = pprint.apply(episode).toString()

  def format(feed: Feed): String = pprint.apply(feed).toString()

  def format(chapter: Chapter): String = pprint.apply(chapter).toString()

  def format(image: Image): String = pprint.apply(image).toString()

  def format(indexDoc: IndexDoc): String = pprint.apply(indexDoc).toString()

  def format(results: ResultsWrapper): String = pprint.apply(results).toString()

  def format(xs: List[Any]): String = pprint.apply(xs).toString()
  */

  def format(podcast: Podcast): String =
    prettyPrint(podcast.copy(
      description = podcast.description.map(truncat),
    ))

  def format(episode: Episode): String =
    prettyPrint(episode.copy(
      description    = episode.description.map(truncat),
      contentEncoded = episode.contentEncoded.map(truncat),
    ))

  def format(feed: Feed): String = prettyPrint(feed)

  def format(chapter: Chapter): String = prettyPrint(chapter)

  def format(image: Image): String = prettyPrint(image)

  def format(doc: IndexDoc): String =
    prettyPrint(doc.copy(
      description    = doc.description.map(truncat),
      chapterMarks   = doc.chapterMarks.map(truncat),
      contentEncoded = doc.contentEncoded.map(truncat),
      transcript     = doc.transcript.map(truncat),
      websiteData    = doc.websiteData.map(truncat),
    ))

  def format(results: ResultsWrapper): String = prettyPrint(results)

  def format(xs: List[Any]): String = prettyPrint(xs)

  private def truncat(value: String): String = value.substring(0, Math.min(value.length, 40)) ++ " ..."

  /**
    * Pretty prints a Scala value similar to its source represention.
    * Particularly useful for case classes.
    * @param a - The value to pretty print.
    * @param indentSize - Number of spaces for each indent.
    * @param maxElementWidth - Largest element size before wrapping.
    * @param depth - Initial depth to pretty print indents.
    * @return
    */
  def prettyPrint(a: Any, indentSize: Int = 2, maxElementWidth: Int = 30, depth: Int = 0): String = {
    val indent = " " * depth * indentSize
    val fieldIndent = indent + (" " * indentSize)
    val thisDepth = prettyPrint(_: Any, indentSize, maxElementWidth, depth)
    val nextDepth = prettyPrint(_: Any, indentSize, maxElementWidth, depth + 1)
    a match {
      // Make Strings look similar to their literal form.
      case s: String =>
        val replaceMap = Seq(
          "\n" -> "\\n",
          "\r" -> "\\r",
          "\t" -> "\\t",
          "\"" -> "\\\""
        )
        '"' + replaceMap.foldLeft(s) { case (acc, (c, r)) => acc.replace(c, r) } + '"'
      // For an empty Seq just use its normal String representation.
      case xs: Seq[_] if xs.isEmpty => xs.toString()
      case xs: Seq[_] =>
        // If the Seq is not too long, pretty print on one line.
        val resultOneLine = xs.map(nextDepth).toString()
        if (resultOneLine.length <= maxElementWidth) return resultOneLine
        // Otherwise, build it with newlines and proper field indents.
        val result = xs.map(x => s"\n$fieldIndent${nextDepth(x)}").toString()
        result.substring(0, result.length - 1) + "\n" + indent + ")"
      // Product should cover case classes.
      case p: Product =>
        val prefix = p.productPrefix
        // We'll use reflection to get the constructor arg names and values.
        val cls = p.getClass
        val fields = cls.getDeclaredFields.filterNot(_.isSynthetic).map(_.getName)
        val values = p.productIterator.toSeq
        // If we weren't able to match up fields/values, fall back to toString.
        if (fields.length != values.length) return p.toString
        fields.zip(values).toList match {
          // If there are no fields, just use the normal String representation.
          case Nil => p.toString
          // If there is just one field, let's just print it as a wrapper.
          case (_, value) :: Nil => s"$prefix(${thisDepth(value)})"
          // If there is more than one field, build up the field names and values.
          case kvps =>
            val prettyFields = kvps.map { case (k, v) => s"$fieldIndent$k = ${nextDepth(v)}" }
            // If the result is not too long, pretty print on one line.
            val resultOneLine = s"$prefix(${prettyFields.mkString(", ")})"
            if (resultOneLine.length <= maxElementWidth) return resultOneLine
            // Otherwise, build it with newlines and proper field indents.
            s"$prefix(\n${prettyFields.mkString(",\n")}\n$indent)"
        }
      // If we haven't specialized this type, just use its toString.
      case _ => a.toString
    }
  }

}
