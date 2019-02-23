package hemin.engine.updater

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import hemin.engine.node.Node.{ActorRefSupervisor, ReportUpdaterInitializationComplete}
import hemin.engine.{HeminConfig, HeminEngine}
import org.scalatest.{FlatSpecLike, Matchers}

class UpdaterSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {

  val engineConfig: HeminConfig = HeminConfig.defaultEngineConfig

  "The Updater" should "report its completed initialization" in {
    val updater: ActorRef = system.actorOf(Updater.props(engineConfig.updater))
    updater ! ActorRefSupervisor(testActor)
    expectMsgType[ReportUpdaterInitializationComplete.type]
  }

}
