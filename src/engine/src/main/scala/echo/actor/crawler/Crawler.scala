package echo.actor.crawler

import java.io.UnsupportedEncodingException
import java.net.{ConnectException, SocketTimeoutException, UnknownHostException}
import java.nio.charset.IllegalCharsetNameException
import javax.net.ssl.SSLHandshakeException

import akka.actor.SupervisorStrategy.{Escalate, Resume}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol.{ActorRefDirectoryStoreActor, ActorRefParserActor}
import echo.core.exception.EchoException

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
    private var workerIndex = 1

    private var parser: ActorRef = _
    private var directory: ActorRef = _

    private var router: Router = {
        val routees = Vector.fill(WORKER_COUNT) {
            val crawler = createCrawler()
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
            case _: Exception                    => Escalate
        }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {
        case ActorRefParserActor(ref) =>
            log.debug("Received ActorRefIndexerActor(_)")
            parser = ref
            router.routees.foreach(r => r.send(ActorRefParserActor(parser), sender()))

        case ActorRefDirectoryStoreActor(ref) =>
            log.debug("Received ActorRefDirectoryStoreActor(_)")
            directory = ref
            router.routees.foreach(r => r.send(ActorRefDirectoryStoreActor(directory), sender()))

        case Terminated(corpse) =>
            //log.info("Child '{}' terminated" + corpse.path.name)

            /* TODO at some point we want to simply restart replace the worker
            router = router.removeRoutee(corpse)
            val crawler = createCrawler()
            context watch crawler
            router = router.addRoutee(crawler)
            */

            /*
            router = router.removeRoutee(corpse)
            if(router.routees.isEmpty) {
                log.info("No more workers available")
                context.stop(self)
            }
            log.info("We do not re-create terminated crawlers for now")
            */

            log.error(s"A ${self.path} worker died : {}", corpse.path.name)
            context.stop(self)

        case PoisonPill =>
            log.debug("Received a PosionPill -> forwarding it to all routees")
            //router.routees.foreach(r => r.send(PoisonPill, sender()))

        case work =>
            log.debug("Routing work of kind : {}", work.getClass)
            router.route(work, sender())
    }

    private def createCrawler(): ActorRef = {
        val crawler = context.actorOf(CrawlerWorker.props(), CrawlerWorker.name(workerIndex))

        workerIndex += 1

        // forward the actor refs to the worker, but only if those references haven't died
        Option(parser).foreach(p => directory ! ActorRefParserActor(p) )
        Option(directory).foreach(d => directory ! ActorRefDirectoryStoreActor(d))

        crawler
    }

}
