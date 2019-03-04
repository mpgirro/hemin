package hemin.engine.graph

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import hemin.engine.node.Node.{ActorRefSupervisor, ReportGraphStoreInitializationComplete}
import hemin.engine.{HeminEngine, TestConstants}
import org.scalatest.{FlatSpecLike, Matchers}

class GraphSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {

  val graphConfig: GraphConfig = TestConstants.engineConfig.graph

  "The GraphStore" should "report its completed initialization" in {
    val graph: ActorRef = system.actorOf(GraphStore.props(graphConfig))
    graph ! ActorRefSupervisor(testActor)
    expectMsgType[ReportGraphStoreInitializationComplete.type]
  }

}
