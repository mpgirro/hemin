package exo.engine

import akka.actor.{Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated}
import akka.pattern.{CircuitBreaker, ask}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import exo.engine.EngineProtocol._
import exo.engine.NodeMaster.{GetCatalogBroker, GetIndexBroker, GetUpdater}
import exo.engine.catalog.{CatalogBroker, CatalogStore}
import exo.engine.catalog.CatalogStore.CatalogMessage
import exo.engine.config.ExoConfig
import exo.engine.crawler.Crawler
import exo.engine.crawler.Crawler.CrawlerMessage
import exo.engine.index.{IndexBroker, IndexStore}
import exo.engine.index.IndexStore.IndexMessage
import exo.engine.parser.Parser
import exo.engine.parser.Parser.ParserMessage
import exo.engine.updater.Updater
import exo.engine.updater.Updater.UpdaterMessage

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */

object NodeMaster {
    final val name = "node"
    def props(config: ExoConfig): Props = Props(new NodeMaster(config))

    case class GetCatalogBroker()
    case class GetIndexBroker()
    case class GetUpdater()
}

class NodeMaster (config: ExoConfig) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    override val supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

    private implicit val executionContext = context.system.dispatcher

    //private val cluster = Cluster(context.system)

    private implicit val INTERNAL_TIMEOUT = config.internalTimeout

    private var index: ActorRef = _
    private var parser: ActorRef = _
    private var crawler: ActorRef = _
    private var catalog: ActorRef = _
    private var updater: ActorRef = _

    override def preStart(): Unit = {

        //val clusterDomainListener = context.watch(context.actorOf(ClusterDomainEventListener.props(), ClusterDomainEventListener.name))

        index   = context.watch(context.actorOf(IndexStore.props(config.indexConfig),     IndexStore.name))
        parser  = context.watch(context.actorOf(Parser.props(config.parserConfig),        Parser.name))
        crawler = context.watch(context.actorOf(Crawler.props(config.crawlerConfig),      Crawler.name))
        catalog = context.watch(context.actorOf(CatalogStore.props(config.catalogConfig), CatalogStore.name))
        updater = context.watch(context.actorOf(Updater.props(config.updaterConfig),      Updater.name))


        // pass around references not provided by constructors due to circular dependencies
        crawler ! ActorRefCatalogStoreActor(catalog)
        crawler ! ActorRefIndexStoreActor(index)
        crawler ! ActorRefParserActor(parser)
        crawler ! ActorRefCatalogStoreActor(catalog)

        parser ! ActorRefCatalogStoreActor(catalog)
        parser ! ActorRefIndexStoreActor(index)
        parser ! ActorRefCrawlerActor(crawler)

        catalog ! ActorRefCatalogStoreActor(catalog)
        catalog ! ActorRefIndexStoreActor(index)
        catalog ! ActorRefCrawlerActor(crawler)
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

        case msg: CatalogMessage => catalog.tell(msg, sender())
        case msg: CrawlerMessage => crawler.tell(msg, sender())
        case msg: IndexMessage   => index.tell(msg, sender())
        case msg: ParserMessage  => parser.tell(msg, sender())
        case msg: UpdaterMessage => updater.tell(msg, sender())

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
        context.system.stop(crawler)    // these have a too full inbox usually to let them finish processing
        context.system.stop(catalog)
        context.system.stop(index)
        context.system.stop(parser)

        //cluster.leave(cluster.selfAddress) // leave the cluster before shutdown

        context.system.terminate().onComplete(_ => log.info("system.terminate() finished"))
        //context.stop(self)  // master

    }

}
