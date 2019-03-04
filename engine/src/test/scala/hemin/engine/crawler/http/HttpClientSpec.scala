package hemin.engine.crawler.http

import hemin.engine.crawler.CrawlerConfig
import hemin.engine.{HeminConfig, HeminException, TestConstants}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.{Failure, Try}

class HttpClientSpec
  extends FlatSpec
    with Matchers
    with BeforeAndAfterAll {

  val crawlerConfig: CrawlerConfig = TestConstants.engineConfig.crawler

  val httpClient = new HttpClient(crawlerConfig.downloadTimeout, crawlerConfig.downloadMaxBytes)

  def ignoreMimeFunc(ignore: String): Boolean = false

  override def afterAll: Unit = {
    httpClient.close()
  }

  "The HEAD check" should "fail on URLs without a scheme separator ('://')" in {
    val result: Try[HttpHeadResult] = httpClient.headCheck("hemin.io/fail.html", ignoreMimeFunc)
    assert(result.isFailure)
  }

  it should "fail if URLs have multiple scheme separators ('://')" in {
    val result: Try[HttpHeadResult] = httpClient.headCheck("https://hemin.io://fail.html", ignoreMimeFunc)
    assert(result.isFailure)
  }

}
