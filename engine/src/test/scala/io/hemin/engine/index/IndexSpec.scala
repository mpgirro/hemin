package io.hemin.engine.index

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import io.hemin.engine.{HeminEngine, TestConstants}
import io.hemin.engine.node.Node.{ActorRefSupervisor, ReportIndexStoreInitializationComplete}
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
