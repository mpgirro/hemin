package hemin.engine.parser.opml

import java.io.StringReader

import com.rometools.opml.feed.opml.Outline
import com.rometools.rome.feed.WireFeed
import com.rometools.rome.io.WireFeedInput
import com.typesafe.scalalogging.Logger

import scala.collection.JavaConverters._
import scala.util.Try

object RomeOpmlParser extends OpmlParserCompanion {
  override def parse(xmlData: String): Try[OpmlParser] = Try(new RomeOpmlParser(xmlData))
}

class RomeOpmlParser private (private val xmlData: String)
  extends OpmlParser {

  private val log: Logger = Logger(getClass)

  private val input = new WireFeedInput
  private val feed: WireFeed = input.build(new StringReader(xmlData))
  private val outlines: List[Outline] = RomeOpmlExtractor.getOutlines(feed).asScala.toList

  override val feedUrls: List[String] = xmlUrls(outlines)

  private def xmlUrls(outlines: Seq[Outline]): List[String] = outlines match {
    case Nil     => Nil
    case o :: os => Option(o.getXmlUrl).toList ++ xmlUrls(o.getChildren.asScala) ++ xmlUrls(os)
  }

}
