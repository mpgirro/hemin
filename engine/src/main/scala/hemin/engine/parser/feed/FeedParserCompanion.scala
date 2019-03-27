package hemin.engine.parser.feed

import scala.util.Try

trait FeedParserCompanion {

  /** Attempts to parse an RSS 2.0 or Atom 1.0 feed.
    *
    * @param xmlData The XML data structure of the feed as a raw String
    * @return The [[hemin.engine.parser.feed.FeedParser]] instance holding
    *         the valuesif the parsing was successful, and a failure otherwise
    */
  def parse(xmlData: String): Try[FeedParser]

}
