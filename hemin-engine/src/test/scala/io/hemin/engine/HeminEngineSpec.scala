package io.hemin.engine

import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.tagobjects.Slow
import org.scalatest.{BeforeAndAfter, FlatSpec, Ignore, Matchers}

import scala.concurrent.Future
import scala.util.{Failure, Success}

// TODO this spec relies on a complete engine setup present (mongo, solr); there should be in memory dummies present

@Ignore // TODO this test is ignored at the moment, because it is slow, spams the output and is not really useful
class HeminEngineSpec
  extends FlatSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfter {

  def newEngine(): HeminEngine = HeminEngine.boot(testContext.engineConfig) match {
    case Success(engine) => engine
    case Failure(ex) =>
      assert(false, s"Failed to startup engine; reason : ${ex.getMessage}")
      null // TODO can I return a better result value (just to please the compiler?)
  }

  var testContext: MongoTestContext = _

  before {
    testContext = new MongoTestContext // also starts the embedded MongoDB
  }

  after {
    testContext.stop() // stops the embedded MongoDB
  }

  "The Engine" should "fail on API calls when it is already started" taggedAs Slow in {
    val engine = newEngine()

    val res = engine.shutdown()
    res shouldBe a [Success[_]]

    val f: Future[String] = engine.cli("ping")
    ScalaFutures.whenReady(f.failed) { ex =>
      ex shouldBe a [HeminException]
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
