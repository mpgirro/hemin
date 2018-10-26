package io.hemin.engine.crawler

import java.io.UnsupportedEncodingException
import java.net.{ConnectException, SocketTimeoutException, UnknownHostException}
import java.nio.charset.{IllegalCharsetNameException, MalformedInputException}

import akka.actor.SupervisorStrategy.{Escalate, Resume}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import io.hemin.engine.Node._
import io.hemin.engine.exception.HeminException
import javax.net.ssl.SSLHandshakeException

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Crawler {
  final val name = "crawler"
  def props(config: CrawlerConfig): Props =
    Props(new Crawler(config))
      .withDispatcher(config.dispatcher)
      .withMailbox(config.mailbox)

  trait CrawlerMessage
  trait FetchJob extends CrawlerMessage
  final case class NewPodcastFetchJob() extends FetchJob
  final case class UpdateEpisodesFetchJob(etag: String, lastMod: String) extends FetchJob
  final case class WebsiteFetchJob() extends FetchJob
  final case class PodcastImageFetchJob() extends FetchJob
  final case class EpisodeImageFetchJob() extends FetchJob

  final case class DownloadWithHeadCheck(id: String, url: String, job: FetchJob) extends CrawlerMessage
  final case class DownloadContent(id: String, url: String, job: FetchJob, encoding: Option[String]) extends CrawlerMessage
}

class Crawler (config: CrawlerConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher : {}", self.path.name, context.props.dispatcher)
  log.debug("{} running with mailbox : {}", self.path.name, context.props.mailbox)

  private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup(config.dispatcher)

  private var workerIndex = 0

  private var catalog: ActorRef = _
  private var parser: ActorRef = _
  private var supervisor: ActorRef = _

  private var workerReportedStartupFinished = 0
  private var router: Router = {
    val routees = Vector.fill(config.workerCount) {
      val crawler = createWorker()
      context watch crawler
      ActorRefRoutee(crawler)
    }
    Router(RoundRobinRoutingLogic(), routees) // TODO hier gibt es vll einen besseren router als roundrobin. balanced mailbox?
  }

  override val supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case _: HeminException                => Resume
      case _: ConnectException             => Resume
      case _: SocketTimeoutException       => Resume
      case _: UnknownHostException         => Resume
      case _: SSLHandshakeException        => Resume
      case _: IllegalCharsetNameException  => Resume
      case _: UnsupportedEncodingException => Resume
      case _: MalformedInputException      => Resume
      case e: Exception                    =>
        log.error("A Worker due to an unhandled exception of class : {}", e.getClass)
        Escalate
    }

  override def postStop: Unit = {
    log.info("{} subsystem shutting down", Crawler.name.toUpperCase)
  }

  override def receive: Receive = {

    case msg @ ActorRefCatalogStoreActor(ref) =>
      log.debug("Received ActorRefCatalogStoreActor(_)")
      catalog = ref
      router.routees.foreach(r => r.send(msg, sender()))

    case msg @ ActorRefParserActor(ref) =>
      log.debug("Received ActorRefParserActor(_)")
      parser = ref
      router.routees.foreach(r => r.send(msg, sender()))

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      reportStartupCompleteIfViable()

    case ReportWorkerStartupComplete =>
      workerReportedStartupFinished += 1
      reportStartupCompleteIfViable()

    case Terminated(corpse) =>
      log.error(s"A ${self.path} worker died : {}", corpse.path.name)
      context.stop(self)

    case PoisonPill =>
      log.debug("Received a PosionPill -> forwarding it to all routees")
    //router.routees.foreach(r => r.send(PoisonPill, sender()))

    case work =>
      log.debug("Routing work of kind : {}", work.getClass)
      router.route(work, sender())
  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
  }

  private def reportStartupCompleteIfViable(): Unit = {
    if (workerReportedStartupFinished == config.workerCount && supervisor != null) {
      supervisor ! ReportCrawlerStartupComplete
    }
  }

  private def createWorker(): ActorRef = {
    workerIndex += 1
    val worker = context.actorOf(CrawlerWorker.props(config), CrawlerWorker.name(workerIndex))

    // forward the actor refs to the worker, but only if those references haven't died
    Option(parser).foreach(p => worker ! ActorRefParserActor(p) )
    Option(catalog).foreach(d => worker ! ActorRefCatalogStoreActor(d))
    worker ! ActorRefSupervisor(self)

    worker
  }

}
