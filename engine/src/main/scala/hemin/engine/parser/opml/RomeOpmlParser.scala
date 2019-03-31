package hemin.engine.parser.opml

import java.io.StringReader

import com.rometools.opml.feed.opml.Outline
import com.rometools.rome.feed.WireFeed
import com.rometools.rome.io.WireFeedInput
import com.typesafe.scalalogging.Logger

import scala.collection.JavaConverters._
import scala.util.Try

class RomeOpmlParser
  extends OpmlParser {

  private val log: Logger = Logger(getClass)

  override def parse(xmlData: String): Try[OpmlParserResult] = Try {
    log.debug("Parsing OPML data")

    val input = new WireFeedInput
    val feed: WireFeed = input.build(new StringReader(xmlData))
    val outlines: List[Outline] = RomeOpmlExtractor.getOutlines(feed).asScala.toList
    val feedUrls: List[String] = xmlUrls(outlines)

    OpmlParserResult(feedUrls)
  }

  private def xmlUrls(outlines: Seq[Outline]): List[String] = outlines match {
    case Nil     => Nil
    case o :: os => Option(o.getXmlUrl).toList ++ xmlUrls(o.getChildren.asScala) ++ xmlUrls(os)
  }

}
