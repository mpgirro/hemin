package hemin.engine.cli

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import hemin.engine.node.Node.{ActorRefSupervisor, ReportCliInitializationComplete}
import hemin.engine.{HeminConfig, HeminEngine, TestConstants}
import org.scalatest.{FlatSpecLike, Matchers}

class CommandLineInterpreterSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {

  val engineConfig: HeminConfig = TestConstants.engineConfig

  "The CommandLineInterpreter" should "report its completed initialization" in {
    val crawler: ActorRef = system.actorOf(CommandLineInterpreter.props(engineConfig))
    crawler ! ActorRefSupervisor(testActor)
    expectMsgType[ReportCliInitializationComplete.type]
  }

}
