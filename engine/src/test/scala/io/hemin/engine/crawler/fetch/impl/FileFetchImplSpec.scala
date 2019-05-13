package io.hemin.engine.crawler.fetch.impl

import io.hemin.engine.TestConstants
import io.hemin.engine.crawler.CrawlerConfig
import io.hemin.engine.crawler.fetch.result.HeadResult
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.util.Try

class FileFetchImplSpec
  extends FlatSpec
    with Matchers
    with BeforeAndAfterAll {

  val crawlerConfig: CrawlerConfig = TestConstants.engineConfig.crawler

  val impl = new FileFetchImpl

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
    val result: Try[HeadResult] = impl.check("http://hemin.io/fail.html", ignoreMimeFunc)
    assert(result.isFailure)
  }

}
