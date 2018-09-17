package exo.engine

import akka.actor.{Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import exo.engine.EngineProtocol._
import exo.engine.NodeMaster.{GetCatalogBroker, GetIndexBroker, GetUpdater}
import exo.engine.catalog.CatalogBroker
import exo.engine.crawler.Crawler
import exo.engine.index.IndexBroker
import exo.engine.parser.Parser
import exo.engine.updater.Updater

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */

object NodeMaster {
    final val name = "node"
    def props(): Props = Props(new NodeMaster())

    case class GetCatalogBroker()
    case class GetIndexBroker()
    case class GetUpdater()
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
    private var crawler: ActorRef = _
    private var catalog: ActorRef = _
    private var updater: ActorRef = _
    private var cli: ActorRef = _

    override def preStart(): Unit = {

        val clusterDomainListener = context.watch(context.actorOf(ClusterDomainEventListener.props(), ClusterDomainEventListener.name))

        index    = context.watch(context.actorOf(IndexBroker.props(),   IndexBroker.name))
        parser   = context.watch(context.actorOf(Parser.props(),        Parser.name(1)))
        crawler  = context.watch(context.actorOf(Crawler.props(),       Crawler.name(1)))
        catalog  = context.watch(context.actorOf(CatalogBroker.props(), CatalogBroker.name))
        updater  = context.watch(context.actorOf(Updater.props(),       Updater.name))


        // pass around references not provided by constructors due to circular dependencies
        crawler ! ActorRefParserActor(parser)
        crawler ! ActorRefCatalogStoreActor(catalog)

        parser ! ActorRefCatalogStoreActor(catalog)
        parser ! ActorRefCrawlerActor(crawler)

        catalog ! ActorRefCrawlerActor(crawler)
        catalog ! ActorRefCatalogStoreActor(catalog)
        catalog ! ActorRefUpdaterActor(updater)

        updater ! ActorRefCatalogStoreActor(catalog)
        updater ! ActorRefCrawlerActor(crawler)

        log.info("up and running")
    }

    override def postStop: Unit = {

        log.info("shutting down")
    }

    override def receive: Receive = {

        case GetCatalogBroker => sender ! catalog

        case GetIndexBroker => sender ! index

        case GetUpdater => sender ! updater

        case Terminated(corpse) => onTerminated(corpse)

        case ShutdownSystem   => onSystemShutdown()
    }

    private def onTerminated(corpse: ActorRef): Unit = {
        log.error("Oh noh! A critical subsystem died : {}", corpse.path)
        self ! ShutdownSystem
    }

    private def onSystemShutdown(): Unit = {
        log.info("initiating shutdown sequence")

        // it is important to shutdown all actor(supervisor) befor shutting down the actor system
        context.system.stop(cli)
        context.system.stop(crawler)    // these have a too full inbox usually to let them finish processing
        context.system.stop(catalog)
        context.system.stop(index)
        context.system.stop(parser)

        cluster.leave(cluster.selfAddress) // leave the cluster before shutdown

        context.system.terminate().onComplete(_ => log.info("system.terminate() finished"))
        //context.stop(self)  // master

    }

}
