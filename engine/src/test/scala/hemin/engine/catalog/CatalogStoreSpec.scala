package hemin.engine.catalog

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import hemin.engine.node.Node.{ActorRefSupervisor, ReportCatalogStoreInitializationComplete}
import hemin.engine.{HeminEngine, MongoTestContext}
import org.scalatest._

class CatalogStoreSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers
    //with BeforeAndAfter
    with BeforeAndAfterAll {

  val supervisorMock: ActorRef = TestProbe.apply().ref

  val testContext: MongoTestContext = new MongoTestContext // also starts the embedded MongoDB

  /*
  before {
    testContext = new MongoTestContext // also starts the embedded MongoDB
  }

  after {
    testContext.stop() // stops the embedded MongoDB
  }
  */

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
    testContext.stop() // stops the embedded MongoDB
  }

  "The CatalogStore" should "report its completed initialization" in {
    val catalog: ActorRef = system.actorOf(CatalogStore.props(testContext.engineConfig.catalog))
    catalog ! ActorRefSupervisor(testActor)
    expectMsgType[ReportCatalogStoreInitializationComplete.type]
  }


}
