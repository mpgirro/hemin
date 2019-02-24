package hemin.engine.parser.opml

import java.nio.file.{Files, Paths}
import java.util.stream.Collectors

import com.typesafe.scalalogging.Logger
import org.scalatest.{FlatSpec, Ignore, Matchers}

import scala.util.{Failure, Success}

@Ignore
class RomeOpmlParserSpec
  extends FlatSpec
    with Matchers {

  private val log: Logger = Logger(getClass)

  private val feedData: String = Files
    .lines(Paths.get("src", "test", "resources", "testopml.xml"))
    .collect(Collectors.joining("\n"))

  private val parseFailureMsg = "A RomeOpmlParser for the test feed data could not be instantiated"

  private val expectedXmlUrls = List(
    "https://example.org/feed1.rss",
    "https://example.org/feed2.rss",
    "https://example.org/feed3.rss"
  )

  "The RomeOpmlParser" should "be able to parse a valid OPML-File" in {
    RomeOpmlParser.parse(feedData) match {
      case Success(_)  => succeed // all is well
      case Failure(ex) => fail(parseFailureMsg)
    }
  }

  it should "extract all expected XML feed URLs" in {
    RomeOpmlParser.parse(feedData) match {
      case Success(parser) => parser.feedUrls should equal (expectedXmlUrls)
      case Failure(_)      => fail(parseFailureMsg)
    }
  }

}
