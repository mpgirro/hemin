package io.hemin.engine.parser

import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.scalalogging.Logger
import io.hemin.engine.node.Node._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Parser {
  final val name = "parser"
  def props(config: ParserConfig): Props =
    Props(new Parser(config))
      .withDispatcher(config.dispatcher)
      .withMailbox(config.mailbox)

  trait ParserMessage
  final case class ParseNewPodcastData(feedUrl: String, podcastId: String, feedData: String) extends ParserMessage
  final case class ParseUpdateEpisodeData(feedUrl: String, podcastId: String, episodeFeedData: String) extends ParserMessage
  final case class ParseWebsiteData(id: String, html: String) extends ParserMessage
  final case class ParseFyydEpisodes(podcastId: String, episodesData: String) extends ParserMessage
  final case class ParseImage(url: String, mime: Option[String], encoding: String, bytes: Array[Byte]) extends ParserMessage
  final case class ParsePodcastImage(podcastId: String, url: String, mime: Option[String], encoding: String, imageData: Array[Byte]) extends ParserMessage
  final case class ParseEpisodeImage(episodeId: String, url: String, mime: Option[String], encoding: String, imageData: Array[Byte]) extends ParserMessage
}

class Parser (config: ParserConfig)
  extends Actor {

  private val log: Logger = Logger(getClass)

  log.debug("{} running on dispatcher : {}", self.path.name, context.system.dispatchers.lookup(context.props.dispatcher))
  log.debug("{} running with mailbox : {}", self.path.name, context.system.mailboxes.lookup(context.props.mailbox))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private var workerIndex = 0

  private var catalog: ActorRef = _
  private var index: ActorRef = _
  private var crawler: ActorRef = _
  private var supervisor: ActorRef = _

  private var workerReportedStartupFinished = 0
  private var router: Router = {
    val routees = Vector.fill(config.workerCount) {
      val parser = createWorker()
      context watch parser
      ActorRefRoutee(parser)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  override val supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
      case _: Exception            => Escalate
    }

  override def postStop: Unit = {
    log.info("{} subsystem shutting down", Parser.name.toUpperCase)
  }

  override def receive: Receive = {

    case msg @ ActorRefCatalogStoreActor(ref) =>
      log.debug("Received ActorRefCatalogStoreActor(_)")
      catalog = ref
      router.routees.foreach(r => r.send(msg, sender()))

    case msg @ ActorRefIndexStoreActor(ref) =>
      log.debug("Received ActorRefIndexStoreActor(_)")
      index = ref
      router.routees.foreach(r => r.send(msg, sender()))

    case msg @ ActorRefCrawlerActor(ref) =>
      log.debug("Received ActorRefCrawlerActor(_)")
      crawler = ref
      router.routees.foreach(r => r.send(msg, sender()))

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      reportStartupCompleteIfViable()

    case ReportWorkerStartupComplete =>
      workerReportedStartupFinished += 1
      reportStartupCompleteIfViable()

    case PoisonPill =>
      log.debug("Received a PosionPill -> forwarding it to all routees")
      router.routees.foreach(r => r.send(PoisonPill, sender()))

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
      supervisor ! ReportParserStartupComplete
    }
  }

  private def createWorker(): ActorRef = {
    workerIndex += 1
    val worker = context.actorOf(ParserWorker.props(config), ParserWorker.name(workerIndex))

    // forward the actor refs to the worker, but only if those references haven't died
    Option(catalog).foreach(d => worker ! ActorRefCatalogStoreActor(d))
    Option(crawler).foreach(c => worker ! ActorRefCrawlerActor(c))
    worker ! ActorRefSupervisor(self)

    worker
  }

}
