package io.hemin.engine.graph

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import io.hemin.engine.{HeminEngine, TestConstants}
import io.hemin.engine.node.Node.{ActorRefSupervisor, ReportGraphStoreInitializationComplete}
import org.scalatest.{FlatSpecLike, Matchers}

class GraphSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {

  val semanticConfig: GraphConfig = TestConstants.engineConfig.graph

  "The SemanticStore" should "report its completed initialization" in {
    val semantic: ActorRef = system.actorOf(GraphStore.props(semanticConfig))
    semantic ! ActorRefSupervisor(testActor)
    expectMsgType[ReportGraphStoreInitializationComplete.type]
  }

}
