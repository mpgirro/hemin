package hemin.engine.parser.opml

import scala.util.Try

trait OpmlParserCompanion {

  /** Attempts to parse an OPML data structure.
    *
    * @param xmlData The XML data structure of the OPML as a raw String
    * @return The [[hemin.engine.parser.opml.OpmlParser]] instance holding
    *         the values if the parsing was successfulm and a failure otherwise
    */
  def parse(xmlData: String): Try[OpmlParser]

}
