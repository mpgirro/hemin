package io.hemin.engine.catalog

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import io.hemin.engine.node.Node.{ActorRefSupervisor, ReportCatalogStoreInitializationComplete}
import io.hemin.engine.{HeminConfig, HeminEngine, TestConstants}
import org.scalatest._

class CatalogStoreSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers
    //with BeforeAndAfter
    with BeforeAndAfterAll {

  val engineConfig: HeminConfig = TestConstants.engineConfig
  val supervisorMock: ActorRef = TestProbe.apply().ref

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "The CatalogStore" should "report its completed initialization" in {
    val catalog: ActorRef = system.actorOf(CatalogStore.props(engineConfig.catalog))
    catalog ! ActorRefSupervisor(testActor)
    expectMsgType[ReportCatalogStoreInitializationComplete.type]
  }


}
