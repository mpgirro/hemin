package exo.engine.crawler

import java.io.UnsupportedEncodingException
import java.net.{ConnectException, SocketTimeoutException, UnknownHostException}
import java.nio.charset.{IllegalCharsetNameException, MalformedInputException}

import javax.net.ssl.SSLHandshakeException
import akka.actor.SupervisorStrategy.{Escalate, Resume}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import exo.engine.EngineProtocol._
import exo.engine.exception.EchoException

import scala.collection.JavaConverters._
import scala.concurrent.duration._

/**
  * @author Maximilian Irro
  */

object Crawler {
    def name(nodeIndex: Int): String = "crawler-" + nodeIndex
    def props(): Props = Props(new Crawler())
}

class Crawler extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val WORKER_COUNT: Int = Option(CONFIG.getInt("echo.crawler.worker-count")).getOrElse(5)
    private var workerIndex = 0

    private var parser: ActorRef = _
    private var catalog: ActorRef = _

    private var router: Router = {
        val routees = Vector.fill(WORKER_COUNT) {
            val crawler = createWorker()
            context watch crawler
            ActorRefRoutee(crawler)
        }
        Router(RoundRobinRoutingLogic(), routees) // TODO hier gibt es vll einen besseren router als roundrobin. balanced mailbox?
    }

    override val supervisorStrategy: SupervisorStrategy =
        OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
            case _: EchoException                => Resume
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
        log.info("shutting down")
    }

    override def receive: Receive = {
        case msg @ ActorRefParserActor(ref) =>
            log.debug("Received ActorRefIndexerActor(_)")
            parser = ref
            router.routees.foreach(r => r.send(msg, sender()))

        case msg @ ActorRefCatalogStoreActor(ref) =>
            log.debug("Received ActorRefCatalogStoreActor(_)")
            catalog = ref
            router.routees.foreach(r => r.send(msg, sender()))

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

    private def createWorker(): ActorRef = {
        workerIndex += 1
        val worker = context.actorOf(CrawlerWorker.props(), CrawlerWorker.name(workerIndex))

        // forward the actor refs to the worker, but only if those references haven't died
        Option(parser).foreach(p => worker ! ActorRefParserActor(p) )
        Option(catalog).foreach(d => worker ! ActorRefCatalogStoreActor(d))
        worker ! ActorRefSupervisor(self)

        worker
    }

}
