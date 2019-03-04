package hemin.engine.index

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import hemin.engine.node.Node.{ActorRefSupervisor, ReportIndexStoreInitializationComplete}
import hemin.engine.{HeminEngine, TestConstants}
import org.scalatest.{FlatSpecLike, Matchers}

class IndexSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {

  val indexConfig: IndexConfig = TestConstants.engineConfig.index

  "The IndexStore" should "report its completed initialization" in {
    val index: ActorRef = system.actorOf(IndexStore.props(indexConfig))
    index ! ActorRefSupervisor(testActor)
    expectMsgType[ReportIndexStoreInitializationComplete.type]
  }

}
