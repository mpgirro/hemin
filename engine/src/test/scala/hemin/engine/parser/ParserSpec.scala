package hemin.engine.parser

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import hemin.engine.node.Node.{ActorRefSupervisor, ReportParserInitializationComplete}
import hemin.engine.{HeminEngine, TestConstants}
import org.scalatest.{FlatSpecLike, Matchers}

class ParserSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {

  val parserConfig: ParserConfig = TestConstants.engineConfig.parser

  "The Parser" should "report its completed initialization" in {
    val parser: ActorRef = system.actorOf(Parser.props(parserConfig))
    parser ! ActorRefSupervisor(testActor)
    expectMsgType[ReportParserInitializationComplete.type]
  }

}
