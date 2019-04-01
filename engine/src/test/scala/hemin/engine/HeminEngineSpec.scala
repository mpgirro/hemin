package hemin.engine

import com.typesafe.config.{Config, ConfigFactory}
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

  val config: Config = ConfigFactory.load(System.getProperty("config.resource", "application.conf"))
  val engineConfig: HeminConfig = HeminConfig.load(config)

  // TODO this config fails with reason : Dispatcher [hemin.engine.node.dispatcher] not configured for path akka://hemin/user/node
  //val engineConfig: HeminConfig = TestConstants.engineConfig

  def newEngine(): HeminEngine = HeminEngine.boot(engineConfig) match {
    case Success(engine) => engine
    case Failure(ex) =>
      fail(s"Failed to startup engine; reason : ${ex.getMessage}")
  }

  "The Engine" should "fail gracefully on API calls when it is already shutdown" taggedAs Slow in {
    val engine = newEngine()

    val res = engine.shutdown()
    res shouldBe a [Success[_]]

    val f: Future[String] = engine.cli("ping")
    ScalaFutures.whenReady(f.failed) { ex =>
      ex shouldBe a [HeminException]
    }
  }

  it should "fail gracefully on consecutive shutdowns" taggedAs Slow in {
    val engine = newEngine()

    val res1 = engine.shutdown()
    res1 shouldBe a [Success[_]]

    val res2 = engine.shutdown()
    res2 shouldBe a [Failure[_]]
  }

}
