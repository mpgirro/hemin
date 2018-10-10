package io.hemin.engine.updater

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.google.common.base.Strings.isNullOrEmpty
import io.hemin.engine.EngineProtocol._
import io.hemin.engine.catalog.CatalogStore.ProposeNewFeed
import io.hemin.engine.crawler.Crawler.{DownloadWithHeadCheck, FetchJob}
import io.hemin.engine.updater.Updater.ProcessFeed

object Updater {
  final val name = "updater"
  def props(config: UpdaterConfig): Props =
    Props(new Updater(config))
      .withDispatcher("hemin.updater.dispatcher")

  trait UpdaterMessage
  case class ProcessFeed(id: String, url: String, job: FetchJob) extends UpdaterMessage
}

class Updater (config: UpdaterConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

  private var catalog: ActorRef = _
  private var crawler: ActorRef = _
  private var supervisor: ActorRef = _

  override def receive: Receive = {

    case ActorRefCatalogStoreActor(ref) =>
      log.debug("Received ActorRefCatalogActor(_)")
      catalog = ref

    case ActorRefCrawlerActor(ref) =>
      log.debug("Received ActorRefCrawlerActor(_)")
      crawler = ref

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      supervisor ! ReportUpdaterStartupComplete

    case ProposeNewFeed(url) =>
      log.debug("Received ProposeNewFeed({})", url)
      log.info("Request to propose : {}", url)
      if (!isNullOrEmpty(url))
        catalog ! ProposeNewFeed(url)

    case ProcessFeed(id, url, job: FetchJob) =>
      log.debug("Received ProcessFeed({},{},{})", id, url, job)
      crawler ! DownloadWithHeadCheck(id, url, job)

  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
  }

}
