package echo.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.actor.cli.CLI
import echo.actor.crawler.Crawler
import echo.actor.directory.DirectoryBroker
import echo.actor.gateway.Gateway
import echo.actor.index.IndexBroker
import echo.actor.parser.Parser
import echo.actor.searcher.SearcherActor

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */

object NodeMaster {
    final val name = "node"
    def props(): Props = Props(new NodeMaster())
}

class NodeMaster extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    override val supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

    private implicit val executionContext = context.system.dispatcher

    private val cluster = Cluster(context.system)

    private val CONFIG = ConfigFactory.load()
    private implicit val INTERNAL_TIMEOUT = Option(CONFIG.getInt("echo.internal-timeout")).getOrElse(5).seconds

    private var index: ActorRef = _
    private var parser: ActorRef = _
    private var searcher: ActorRef = _
    private var crawler: ActorRef = _
    private var directory: ActorRef = _
    private var gateway: ActorRef = _
    private var cli: ActorRef = _

    override def preStart(): Unit = {

        val clusterDomainListener = context.watch(context.actorOf(ClusterDomainEventListener.props(), ClusterDomainEventListener.name))

        index = context.watch(context.actorOf(IndexBroker.props(), IndexBroker.name))

        parser = context.actorOf(Parser.props(), Parser.name(1))
        context watch parser

        searcher = context.watch(context.actorOf(SearcherActor.props(), SearcherActor.name))

        crawler = context.actorOf(Crawler.props(), Crawler.name(1))
        context watch crawler

        directory = context.actorOf(DirectoryBroker.props(), DirectoryBroker.name)
        context watch directory

        gateway = context.watch(context.actorOf(Gateway.props(), Gateway.name(1)))

        createCLI()

        // pass around references not provided by constructors due to circular dependencies
        crawler ! ActorRefParserActor(parser)
        crawler ! ActorRefDirectoryStoreActor(directory)

        parser ! ActorRefDirectoryStoreActor(directory)
        parser ! ActorRefCrawlerActor(crawler)

        searcher ! ActorRefIndexStoreActor(index)

        gateway ! ActorRefDirectoryStoreActor(directory)

        directory ! ActorRefCrawlerActor(crawler)
        directory ! ActorRefDirectoryStoreActor(directory)

        log.info("up and running")
    }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {
        case Terminated(corpse) => onTerminated(corpse)
        case ShutdownSystem()   => onSystemShutdown()
    }

    private def onTerminated(corpse: ActorRef): Unit = {
        if (corpse == cli) {
            createCLI() // we simply re-create the CLI
        } else {
            log.error("Oh noh! A critical subsystem died : {}", corpse.path)
            self ! ShutdownSystem()
        }
    }

    private def onSystemShutdown(): Unit = {
        log.info("initiating shutdown sequence")

        // it is important to shutdown all actor(supervisor) befor shutting down the actor system
        context.system.stop(cli)
        context.system.stop(crawler)    // these have a too full inbox usually to let them finish processing
        context.system.stop(directory)
        context.system.stop(gateway)
        context.system.stop(index)
        context.system.stop(parser)
        context.system.stop(searcher)

        cluster.leave(cluster.selfAddress) // leave the cluster before shutdown

        context.system.terminate().onComplete(_ => log.info("system.terminate() finished"))
        //context.stop(self)  // master

    }

    private def createCLI(): Unit = {
        cli = context.actorOf(CLI.props(self, parser, searcher, crawler, directory, gateway),
            name = CLI.name)
        context watch cli
    }

}
