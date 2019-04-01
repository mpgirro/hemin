package hemin.engine.updater

import akka.actor.{Actor, ActorRef, Props}
import com.google.common.base.Strings.isNullOrEmpty
import com.typesafe.scalalogging.Logger
import hemin.engine.catalog.CatalogStore.ProposeNewFeed
import hemin.engine.crawler.Crawler.{DownloadWithHeadCheck, FetchJob}
import hemin.engine.node.Node.{ActorRefCatalogStoreActor, ActorRefCrawlerActor, ActorRefSupervisor, ReportUpdaterInitializationComplete}
import hemin.engine.updater.Updater.ProcessFeed

import scala.concurrent.ExecutionContext

object Updater {
  final val name = "updater"
  def props(config: UpdaterConfig): Props =
    Props(new Updater(config))
      .withDispatcher(config.dispatcher)
      .withMailbox(config.mailbox)

  trait UpdaterMessage
  final case class ProcessFeed(id: String, url: String, job: FetchJob) extends UpdaterMessage
}

class Updater (config: UpdaterConfig)
  extends Actor {

  private val log: Logger = Logger(getClass)

  log.debug("{} running on dispatcher : {}", self.path.name, context.system.dispatchers.lookup(context.props.dispatcher))
  log.debug("{} running with mailbox : {}", self.path.name, context.system.mailboxes.lookup(context.props.mailbox))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private var catalog: ActorRef = _
  private var crawler: ActorRef = _
  private var supervisor: ActorRef = _

  override def postStop: Unit = {
    log.info("{} subsystem shutting down", Updater.name.toUpperCase)
  }

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
      supervisor ! ReportUpdaterInitializationComplete

    case ProposeNewFeed(url) =>
      log.debug("Received ProposeNewFeed({})", url)
      log.info("Request to propose : {}", url)

      if (!isNullOrEmpty(url)) {
        catalog ! ProposeNewFeed(url)
      }

    case ProcessFeed(id, url, job: FetchJob) =>
      log.debug("Received ProcessFeed({},{},{})", id, url, job)
      crawler ! DownloadWithHeadCheck(id, url, job)

  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
  }

}
