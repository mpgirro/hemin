package hemin.engine.searcher

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import hemin.engine.node.Node.{ActorRefSupervisor, ReportSearcherInitializationComplete}
import hemin.engine.{HeminConfig, HeminEngine}
import org.scalatest.{FlatSpecLike, Matchers}

class SearcherSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {

  val engineConfig: HeminConfig = HeminConfig.defaultEngineConfig

  "The Searcher" should "report its completed initialization" in {
    val searcher: ActorRef = system.actorOf(Searcher.props(engineConfig.searcher))
    searcher ! ActorRefSupervisor(testActor)
    expectMsgType[ReportSearcherInitializationComplete.type]
  }

}
