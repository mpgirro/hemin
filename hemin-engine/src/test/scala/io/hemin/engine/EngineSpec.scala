package io.hemin.engine

import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import com.typesafe.config.{Config, ConfigFactory}
import io.hemin.engine.exception.HeminException
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.{Failure, Success}

// TODO this spec relies on a complete engine setup present (mongo, solr); there should be in memory dummies present

class EngineSpec
  extends FlatSpec
    with Matchers
    with ScalaFutures
    with MongoEmbedDatabase
    with BeforeAndAfter {

  def defaultConfig(mongoUri: String): Config = ConfigFactory
    .parseMap(Map("hemin.catalog.mongo-uri" -> mongoUri).asJava)
    .withFallback(EngineConfig.defaultConfig)

  def mongoHost: String = mongoProps.mongodProcess.getConfig.net().getServerAddress.getCanonicalHostName
  def mongoPort: Int = mongoProps.mongodProcess.getConfig.net().getPort
  def mongoUri: String =  s"mongodb://$mongoHost:$mongoPort/${Engine.name}"

  def newEngine(): Engine = new Engine(defaultConfig(mongoUri))

  var mongoProps: MongodProps = null

  before {
    mongoProps = mongoStart()   // by default port = 12345 & version = Version.3.3.1
  }                               // add your own port & version parameters in mongoStart method if you need it

  after { mongoStop(mongoProps) }

  "The Engine" should "fail on API calls when it is already started" in {
    val engine = newEngine()

    val res = engine.shutdown()
    res shouldBe a [Success[_]]

    val f: Future[String] = engine.cli("ping")
    ScalaFutures.whenReady(f.failed) { ex =>
      ex shouldBe a [HeminException]
    }
  }

  it should "fail on consecutive shutdowns" in {
    val engine = newEngine()

    val res1 = engine.shutdown()
    res1 shouldBe a [Success[_]]

    val res2 = engine.shutdown()
    res2 shouldBe a [Failure[_]]
  }

}
