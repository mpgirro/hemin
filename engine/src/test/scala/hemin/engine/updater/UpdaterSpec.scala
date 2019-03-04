package hemin.engine.updater

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import hemin.engine.node.Node.{ActorRefSupervisor, ReportUpdaterInitializationComplete}
import hemin.engine.{HeminEngine, TestConstants}
import org.scalatest.{FlatSpecLike, Matchers}

class UpdaterSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {

  val updaterConfig: UpdaterConfig = TestConstants.engineConfig.updater

  "The Updater" should "report its completed initialization" in {
    val updater: ActorRef = system.actorOf(Updater.props(updaterConfig))
    updater ! ActorRefSupervisor(testActor)
    expectMsgType[ReportUpdaterInitializationComplete.type]
  }

}
