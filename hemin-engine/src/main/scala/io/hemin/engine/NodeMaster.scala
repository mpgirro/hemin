package io.hemin.engine

import akka.actor.{Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated}
import akka.util.Timeout
import io.hemin.engine.EngineProtocol._
import io.hemin.engine.NodeMaster._
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.catalog.CatalogStore.CatalogMessage
import io.hemin.engine.crawler.Crawler
import io.hemin.engine.crawler.Crawler.CrawlerMessage
import io.hemin.engine.index.IndexStore
import io.hemin.engine.index.IndexStore.IndexMessage
import io.hemin.engine.parser.Parser
import io.hemin.engine.parser.Parser.ParserMessage
import io.hemin.engine.searcher.Searcher
import io.hemin.engine.searcher.Searcher.SearcherMessage
import io.hemin.engine.updater.Updater
import io.hemin.engine.updater.Updater.UpdaterMessage
import io.hemin.engine.util.cli.CliProcessor

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps

object NodeMaster {
  final val name = "node"
  def props(config: EngineConfig): Props = Props(new NodeMaster(config))

  final case class CliInput(input: String)
  final case class CliOutput(output: String)
}

class NodeMaster (config: EngineConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

  override val supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

  private implicit val executionContext: ExecutionContextExecutor = context.system.dispatcher

  //private val cluster = Cluster(context.system)

  private implicit val INTERNAL_TIMEOUT: Timeout = config.internalTimeout

  private val processor = new CliProcessor(self, config, executionContext)

  private var index: ActorRef = _
  private var catalog: ActorRef = _
  private var crawler: ActorRef = _
  private var parser: ActorRef = _
  private var searcher: ActorRef = _
  private var updater: ActorRef = _

  private var indexStartupComplete = false
  private var catalogStartupComplete = false
  private var crawlerStartupComplete = false
  private var parserStartupComplete = false
  private var searcherStartupComplete = false
  private var updaterStartupComplete = false

  override def preStart(): Unit = {

    //val clusterDomainListener = context.watch(context.actorOf(ClusterDomainEventListener.props(), ClusterDomainEventListener.name))

    index    = context.watch(context.actorOf(IndexStore.props(config.index),     IndexStore.name))
    parser   = context.watch(context.actorOf(Parser.props(config.parser),        Parser.name))
    crawler  = context.watch(context.actorOf(Crawler.props(config.crawler),      Crawler.name))
    catalog  = context.watch(context.actorOf(CatalogStore.props(config.catalog), CatalogStore.name))
    searcher = context.watch(context.actorOf(Searcher.props(config.searcher),    Searcher.name))
    updater  = context.watch(context.actorOf(Updater.props(config.updater),      Updater.name))


    // pass around references not provided by constructors due to circular dependencies
    index ! ActorRefSupervisor(self)

    crawler ! ActorRefCatalogStoreActor(catalog)
    crawler ! ActorRefIndexStoreActor(index)
    crawler ! ActorRefParserActor(parser)
    crawler ! ActorRefCatalogStoreActor(catalog)
    crawler ! ActorRefSupervisor(self)

    parser ! ActorRefCatalogStoreActor(catalog)
    parser ! ActorRefIndexStoreActor(index)
    parser ! ActorRefCrawlerActor(crawler)
    parser ! ActorRefSupervisor(self)

    catalog ! ActorRefCatalogStoreActor(catalog)
    catalog ! ActorRefIndexStoreActor(index)
    catalog ! ActorRefCrawlerActor(crawler)
    catalog ! ActorRefUpdaterActor(updater)
    catalog ! ActorRefSupervisor(self)

    searcher ! ActorRefSupervisor(self)

    updater ! ActorRefCatalogStoreActor(catalog)
    updater ! ActorRefCrawlerActor(crawler)
    updater ! ActorRefSupervisor(self)
  }

  override def postStop: Unit = {

    log.info("shutting down")
  }

  override def receive: Receive = {

    case CliInput(input) => onReplInput(input, sender)

    case msg: CatalogMessage  => catalog.tell(msg, sender())
    case msg: CrawlerMessage  => crawler.tell(msg, sender())
    case msg: IndexMessage    => index.tell(msg, sender())
    case msg: ParserMessage   => parser.tell(msg, sender())
    case msg: SearcherMessage => searcher.tell(msg, sender())
    case msg: UpdaterMessage  => updater.tell(msg, sender())

    case ReportCatalogStoreStartupComplete =>
      log.info("Catalog reported startup complete")
      catalogStartupComplete = true
    case ReportIndexStoreStartupComplete   =>
      log.info("Index reported startup complete")
      indexStartupComplete = true
    case ReportCrawlerStartupComplete      =>
      log.info("Crawler reported startup complete")
      crawlerStartupComplete = true
    case ReportParserStartupComplete       =>
      log.info("Parser reported startup complete")
      parserStartupComplete = true
    case ReportSearcherStartupComplete       =>
      log.info("Searcher reported startup complete")
      searcherStartupComplete = true
    case ReportUpdaterStartupComplete      =>
      log.info("Updater reported startup complete")
      updaterStartupComplete = true

    case EngineOperational =>
      if (isEngineOperational)
        sender ! StartupComplete
      else
        sender ! StartupInProgress

    case Terminated(corpse) => onTerminated(corpse)

    case ShutdownSystem   => onSystemShutdown()
  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
  }

  private def isEngineOperational: Boolean =
    catalogStartupComplete && indexStartupComplete && crawlerStartupComplete && parserStartupComplete && searcherStartupComplete && updaterStartupComplete

  private def onReplInput(input: String, theSender: ActorRef): Unit = Future {
    theSender ! CliOutput(processor.eval(input))
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
    context.system.stop(parser)
    context.system.stop(index)
    context.system.stop(searcher)
    context.system.stop(updater)

    //cluster.leave(cluster.selfAddress) // leave the cluster before shutdown

    context.system.terminate().onComplete(_ => log.info("system.terminate() finished"))
    //context.stop(self)  // master

  }

}
