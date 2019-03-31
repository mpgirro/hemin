package hemin.engine.parser.opml

import scala.util.Try

trait OpmlParser {

  /** Attempts to parse an OPML data structure.
    *
    * @param xmlData The XML data structure of the OPML as a raw String
    * @return The [[hemin.engine.parser.opml.OpmlParserResult]] instance holding
    *         the values if the parsing was successful, and a failure otherwise
    */
  def parse(xmlData: String): Try[OpmlParserResult]

}
