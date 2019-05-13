package io.hemin.engine.crawler

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import io.hemin.engine.{HeminEngine, TestConstants}
import io.hemin.engine.node.Node.{ActorRefSupervisor, ReportCrawlerInitializationComplete}
import org.scalatest.{FlatSpecLike, Matchers}

class CrawlerSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {

  val crawlerConfig: CrawlerConfig = TestConstants.engineConfig.crawler

  "The Crawler" should "report its completed initialization" in {
    val crawler: ActorRef = system.actorOf(Crawler.props(crawlerConfig))
    crawler ! ActorRefSupervisor(testActor)
    expectMsgType[ReportCrawlerInitializationComplete.type]
  }

}
