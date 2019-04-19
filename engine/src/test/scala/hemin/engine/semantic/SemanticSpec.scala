package hemin.engine.semantic

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import hemin.engine.node.Node.{ActorRefSupervisor, ReportSemanticStoreInitializationComplete}
import hemin.engine.{HeminEngine, TestConstants}
import org.scalatest.{FlatSpecLike, Matchers}

class SemanticSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {

  val semanticConfig: SemanticConfig = TestConstants.engineConfig.semantic

  "The SemanticStore" should "report its completed initialization" in {
    val semantic: ActorRef = system.actorOf(SemanticStore.props(semanticConfig))
    semantic ! ActorRefSupervisor(testActor)
    expectMsgType[ReportSemanticStoreInitializationComplete.type]
  }

}
