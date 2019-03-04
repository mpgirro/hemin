package hemin.engine.index

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import hemin.engine.node.Node.{ActorRefSupervisor, ReportIndexStoreInitializationComplete}
import hemin.engine.{HeminConfig, HeminEngine}
import org.scalatest.{FlatSpecLike, Matchers}

class IndexSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {

  val engineConfig: HeminConfig = HeminConfig.defaultEngineConfig

  "The IndexStore" should "report its completed initialization" in {
    val index: ActorRef = system.actorOf(IndexStore.props(engineConfig.index))
    index ! ActorRefSupervisor(testActor)
    expectMsgType[ReportIndexStoreInitializationComplete.type]
  }

}
