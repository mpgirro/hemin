package io.hemin.engine.crawler.fetch

import io.hemin.engine.TestConstants
import io.hemin.engine.crawler.CrawlerConfig
import io.hemin.engine.crawler.fetch.result.HeadResult
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.Try

class FetcherSpec
  extends FlatSpec
    with Matchers
    with BeforeAndAfterAll {

  val crawlerConfig: CrawlerConfig = TestConstants.engineConfig.crawler

  val fetcher = new Fetcher(crawlerConfig.downloadTimeout, crawlerConfig.downloadMaxBytes)

  def ignoreMimeFunc(ignore: String): Boolean = false

  override def afterAll: Unit = {
    fetcher.close()
  }

  "The Fetcher" should "fail on checks for URLs without a scheme separator ('://')" in {
    val result: Try[HeadResult] = fetcher.check("hemin.io/fail.html", ignoreMimeFunc)
    assert(result.isFailure)
  }

  it should "fail on checks if URLs have multiple scheme separators ('://')" in {
    val result: Try[HeadResult] = fetcher.check("https://hemin.io://fail.html", ignoreMimeFunc)
    assert(result.isFailure)
  }

}
