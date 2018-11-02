package io.hemin.engine

import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.tagobjects.Slow
import org.scalatest.{BeforeAndAfter, FlatSpec, Ignore, Matchers}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.{Failure, Success}

// TODO this spec relies on a complete engine setup present (mongo, solr); there should be in memory dummies present

@Ignore // TODO this test is ignored at the moment, because it is slow, spams the output and is not really useful
class EngineSpec
  extends FlatSpec
    with Matchers
    with ScalaFutures
    with MongoEmbedDatabase
    with BeforeAndAfter {

  def defaultConfig(mongoUri: String): Config = ConfigFactory
    .parseMap(Map("hemin.catalog.mongo-uri" -> mongoUri).asJava)
    .withFallback(EngineConfig.defaultConfig)

  var mongoProps: MongodProps = null

  def mongoHost: String = Option(mongoProps)
    .map(_.mongodProcess)
    .map(_.getConfig)
    .map(_.net)
    .map(_.getServerAddress)
    .map(_.getCanonicalHostName)
    .getOrElse("localhost")
  def mongoPort: Int = Option(mongoProps)
    .map(_.mongodProcess)
    .map(_.getConfig)
    .map(_.net)
    .map(_.getPort)
    .getOrElse(12345)
  def mongoUri: String =  s"mongodb://$mongoHost:$mongoPort/${Engine.name}"

  def newEngine(): Engine = Engine.boot(defaultConfig(mongoUri)) match {
    case Success(e)  => e
    case Failure(ex) =>
      assert(false, s"Failed to startup engine; reason : ${ex.getMessage}")
      null // TODO can I return a better result value (just to please the compiler?)
  }

  before {
    mongoProps = mongoStart()
  }

  after {
    mongoStop(mongoProps)
  }

  "The Engine" should "fail on API calls when it is already started" taggedAs Slow in {
    val engine = newEngine()

    val res = engine.shutdown()
    res shouldBe a [Success[_]]

    val f: Future[String] = engine.cli("ping")
    ScalaFutures.whenReady(f.failed) { ex =>
      ex shouldBe a [EngineException]
    }
  }

  it should "fail on consecutive shutdowns" taggedAs Slow in {
    val engine = newEngine()

    val res1 = engine.shutdown()
    res1 shouldBe a [Success[_]]

    val res2 = engine.shutdown()
    res2 shouldBe a [Failure[_]]
  }

}
