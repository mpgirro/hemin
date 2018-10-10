package io.disposia.engine.parser

import akka.actor.SupervisorStrategy.{Escalate, Resume}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import io.disposia.engine.EngineProtocol._
import io.disposia.engine.exception.FeedParsingException

import scala.concurrent.duration._

object Parser {
  final val name = "parser"
  def props(config: ParserConfig): Props =
    Props(new Parser(config))
      .withDispatcher("echo.parser.dispatcher")

  trait ParserMessage
  case class ParseNewPodcastData(feedUrl: String, podcastId: String, feedData: String) extends ParserMessage
  case class ParseUpdateEpisodeData(feedUrl: String, podcastId: String, episodeFeedData: String) extends ParserMessage
  case class ParseWebsiteData(id: String, html: String) extends ParserMessage
  case class ParseFyydEpisodes(podcastId: String, episodesData: String) extends ParserMessage
  case class ParsePodcastImage(podcastId: String, imageData: String) extends ParserMessage
  case class ParseEpisodeImage(episodeId: String, imageData: String) extends ParserMessage
}

class Parser (config: ParserConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

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
      case _: FeedParsingException => Resume
      case _: Exception            => Escalate
    }

  override def postStop: Unit = {
    log.info("shutting down")
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
