package hemin.engine.crawler.fetch.impl

import hemin.engine.TestConstants
import hemin.engine.crawler.CrawlerConfig
import hemin.engine.crawler.fetch.result.HeadResult
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.util.Try

class HttpFetchImplSpec
  extends FlatSpec
    with Matchers
    with BeforeAndAfterAll {

  val crawlerConfig: CrawlerConfig = TestConstants.engineConfig.crawler

  val impl = new HttpFetchImpl(crawlerConfig.downloadTimeout.seconds, crawlerConfig.downloadMaxBytes)

  def ignoreMimeFunc(ignore: String): Boolean = false

  override def afterAll: Unit = {
    impl.close()
  }

  "The HttpFetchImpl" should "fail on checks for URLs without a scheme separator ('://')" in {
    val result: Try[HeadResult] = impl.check("hemin.io/fail.html", ignoreMimeFunc)
    assert(result.isFailure)
  }

  it should "fail on checks if URLs have multiple scheme separators ('://')" in {
    val result: Try[HeadResult] = impl.check("https://hemin.io://fail.html", ignoreMimeFunc)
    assert(result.isFailure)
  }

  it should "fail on checks for URLs with an unsupported URI scheme" in {
    val result: Try[HeadResult] = impl.check("file:///hemin.io/fail.html", ignoreMimeFunc)
    assert(result.isFailure)
  }

}
