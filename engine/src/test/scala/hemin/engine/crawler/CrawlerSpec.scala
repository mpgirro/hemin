package hemin.engine.crawler

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import hemin.engine.{HeminConfig, HeminEngine}
import hemin.engine.node.Node.{ActorRefSupervisor, ReportCrawlerInitializationComplete}
import org.scalatest.{FlatSpecLike, Matchers}

class CrawlerSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {

  val engineConfig: HeminConfig = HeminConfig.defaultEngineConfig

  "The Crawler" should "report its completed initialization" in {
    val crawler: ActorRef = system.actorOf(Crawler.props(engineConfig.crawler))
    crawler ! ActorRefSupervisor(testActor)
    expectMsgType[ReportCrawlerInitializationComplete.type]
  }

}
