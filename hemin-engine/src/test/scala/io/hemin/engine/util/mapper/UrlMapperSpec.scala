package io.hemin.engine.util.mapper

import org.scalatest.{FlatSpec, Matchers}

class UrlMapperSpec extends FlatSpec with Matchers {

  private val mapper = UrlMapper

  "The UrlMapper" should "remove the protocol" in {
    mapper.keywords("http://") shouldBe ""
    mapper.keywords("HTTP://") shouldBe ""
    mapper.keywords("https://") shouldBe ""
    mapper.keywords("HTTPS://") shouldBe ""
  }

  it should "remove dots symbols ('.')" in {
    mapper.keywords("ab.cd.ef") shouldBe "ab cd ef"
  }

  it should "remove plus symbols ('+')" in {
    mapper.keywords("ab+cd") shouldBe "ab cd"
  }

  it should "remove minus symbols ('-')" in {
    mapper.keywords("ab-cd") shouldBe "ab cd"
  }

  it should "remove userinfo separator ('@')" in {
    mapper.keywords("ab@cd") shouldBe "ab cd"
  }

  it should "remove host subcomponent left enclosure ('[')" in {
    mapper.keywords("ab[cd") shouldBe "ab cd"
  }

  it should "remove host subcomponent right enclosure (']')" in {
    mapper.keywords("ab]cd") shouldBe "ab cd"
  }

  it should "remove all slashes ('/')" in {
    mapper.keywords("ab/cd/ef/") shouldBe "ab cd ef"
  }

  it should "remove the query separator ('?')" in {
    mapper.keywords("ab?cd") shouldBe "ab cd"
  }

  it should "remove the fragment separator ('#')" in {
    mapper.keywords("ab#cd") shouldBe "ab cd"
  }

  it should "remove the parameter separator ('&')" in {
    mapper.keywords("ab#cd") shouldBe "ab cd"
  }

  it should "remove the parameter name/value separators ('=')" in {
    mapper.keywords("ab=cd") shouldBe "ab cd"
  }

  it should "remove matrix parameter separators (';')" in {
    mapper.keywords("ab;cd") shouldBe "ab cd"
  }

  it should "trim and reduce long whitespaces" in {
    mapper.keywords(" a  B c  d   ") shouldBe "a B c d"
  }

}
