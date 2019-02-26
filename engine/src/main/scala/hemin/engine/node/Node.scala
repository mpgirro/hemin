package hemin.engine.node

import akka.actor.{Actor, ActorRef, Props, SupervisorStrategy, Terminated}
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import hemin.engine.HeminConfig
import hemin.engine.catalog.CatalogStore
import hemin.engine.catalog.CatalogStore.CatalogMessage
import hemin.engine.cli.CommandLineInterpreter
import hemin.engine.cli.CommandLineInterpreter.CliMessage
import hemin.engine.crawler.Crawler
import hemin.engine.crawler.Crawler.CrawlerMessage
import hemin.engine.graph.GraphStore
import hemin.engine.graph.GraphStore.GraphStoreMessage
import hemin.engine.index.IndexStore
import hemin.engine.index.IndexStore.IndexMessage
import hemin.engine.node.Node._
import hemin.engine.parser.Parser
import hemin.engine.parser.Parser.ParserMessage
import hemin.engine.searcher.Searcher
import hemin.engine.searcher.Searcher.SearcherMessage
import hemin.engine.updater.Updater
import hemin.engine.updater.Updater.UpdaterMessage
import hemin.engine.util.InitializationProgress

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

object Node {
  final val name = "node"
  def props(config: HeminConfig): Props =
    Props(new Node(config))
      .withDispatcher(config.node.dispatcher)
      //.withMailbox(config.node.mailbox) // TODO why is this causing chaos and madness on App startup?!

  final case class CliInput(input: String)
  final case class CliOutput(output: String)

  // These messages are sent to propagate actorRefs to other actors, to overcome circular dependencies
  trait ActorRefInfo
  final case class ActorRefCatalogStoreActor(ref: ActorRef) extends ActorRefInfo
  final case class ActorRefCrawlerActor(ref: ActorRef) extends ActorRefInfo
  final case class ActorRefParserActor(ref: ActorRef) extends ActorRefInfo
  final case class ActorRefIndexStoreActor(ref: ActorRef) extends ActorRefInfo
  final case class ActorRefUpdaterActor(ref: ActorRef) extends ActorRefInfo
  final case class ActorRefSupervisor(ref: ActorRef) extends ActorRefInfo

  // Startup sequence messages
  final case object ReportCatalogStoreInitializationComplete
  final case object ReportIndexStoreInitializationComplete
  final case object ReportCliInitializationComplete
  final case object ReportCrawlerInitializationComplete
  final case object ReportGraphStoreInitializationComplete
  final case object ReportParserInitializationComplete
  final case object ReportSearcherInitializationComplete
  final case object ReportUpdaterInitializationComplete
  final case object ReportWorkerInitializationComplete // for worker/handler delegation children

  // Startup protocol messags
  final case class EngineOperational()
  final case class StartupStatus(complete: Boolean)

  // These are maintenance methods, I use during development
  trait DebugMessage
  final case class DebugPrintAllPodcasts() extends DebugMessage
  final case class DebugPrintAllEpisodes() extends DebugMessage
  final case class DebugPrintAllFeeds() extends DebugMessage

  // User -> Crawler
  // TODO: automatic: Crawler -> Crawler on a regular basis
  trait CrawlExternalDirectory
  final case class CrawlFyyd(count: Int) extends CrawlExternalDirectory
  final case class LoadFyydEpisodes(podcastId: String, fyydId: Long) extends CrawlExternalDirectory

  // CLI -> Master
  final case class ShutdownSystem()
}

class Node(config: HeminConfig)
  extends Actor {

  private val log: Logger = Logger(getClass)

  log.debug("{} running on dispatcher : {}", self.path.name, context.system.dispatchers.lookup(context.props.dispatcher))
  log.debug("{} running with mailbox : {}", self.path.name, context.system.mailboxes.lookup(context.props.mailbox))

  override val supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

  private implicit val executionContext: ExecutionContext = context.dispatcher
  private implicit val internalTimeout: Timeout = config.node.internalTimeout

  //private val cluster = Cluster(context.system)

  //private val cli = new CommandLineInterpreter(self, config, executionContext)

  private val initializationProgress = new InitializationProgress(Set(
    CatalogStore.name,
    CommandLineInterpreter.name,
    Crawler.name,
    GraphStore.name,
    IndexStore.name,
    Parser.name,
    Searcher.name,
    Updater.name
  ))

  private var index: ActorRef = _
  private var catalog: ActorRef = _
  private var cli: ActorRef = _
  private var crawler: ActorRef = _
  private var parser: ActorRef = _
  private var searcher: ActorRef = _
  private var updater: ActorRef = _
  private var graph: ActorRef = _

  override def postRestart(cause: Throwable): Unit = {
    log.warn("{} has been restarted or resumed", self.path.name)
    cause match {
      case e: Exception =>
        log.error("Unhandled Exception : {}", e.getMessage, e)
    }
    super.postRestart(cause)
  }

  override def preStart(): Unit = {

    //val clusterDomainListener = context.watch(context.actorOf(ClusterDomainEventListener.props(), ClusterDomainEventListener.name))

    index    = context.watch(context.actorOf(IndexStore.props(config.index),       IndexStore.name))
    parser   = context.watch(context.actorOf(Parser.props(config.parser),          Parser.name))
    crawler  = context.watch(context.actorOf(Crawler.props(config.crawler),        Crawler.name))
    catalog  = context.watch(context.actorOf(CatalogStore.props(config.catalog),   CatalogStore.name))
    searcher = context.watch(context.actorOf(Searcher.props(config.searcher),      Searcher.name))
    updater  = context.watch(context.actorOf(Updater.props(config.updater),        Updater.name))
    cli      = context.watch(context.actorOf(CommandLineInterpreter.props(config), CommandLineInterpreter.name))
    graph    = context.watch(context.actorOf(GraphStore.props(config.graph),       GraphStore.name))


    // pass around references not provided by constructors due to circular dependencies
    index ! ActorRefSupervisor(self)

    cli ! ActorRefSupervisor(self)

    crawler ! ActorRefCatalogStoreActor(catalog)
    crawler ! ActorRefParserActor(parser)
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

    graph ! ActorRefSupervisor(self)
  }

  override def postStop: Unit = {
    log.info("{} shutting down", Node.name.toUpperCase)
  }

  override def receive: Receive = {

    case msg: CatalogMessage    => catalog.tell(msg, sender())
    case msg: CliMessage        => cli.tell(msg, sender())
    case msg: CrawlerMessage    => crawler.tell(msg, sender())
    case msg: GraphStoreMessage => graph.tell(msg, sender())
    case msg: IndexMessage      => index.tell(msg, sender())
    case msg: ParserMessage     => parser.tell(msg, sender())
    case msg: SearcherMessage   => searcher.tell(msg, sender())
    case msg: UpdaterMessage    => updater.tell(msg, sender())

    case ReportCatalogStoreInitializationComplete => initializationProgress.signalCompletion(CatalogStore.name)
    case ReportCliInitializationComplete          => initializationProgress.signalCompletion(CommandLineInterpreter.name)
    case ReportCrawlerInitializationComplete      => initializationProgress.signalCompletion(Crawler.name)
    case ReportGraphStoreInitializationComplete   => initializationProgress.signalCompletion(GraphStore.name)
    case ReportIndexStoreInitializationComplete   => initializationProgress.signalCompletion(IndexStore.name)
    case ReportParserInitializationComplete       => initializationProgress.signalCompletion(Parser.name)
    case ReportSearcherInitializationComplete     => initializationProgress.signalCompletion(Searcher.name)
    case ReportUpdaterInitializationComplete      => initializationProgress.signalCompletion(Updater.name)

    case EngineOperational =>
      if (initializationProgress.isFinished) {
        sender ! StartupStatus(complete = true)
      } else {
        sender ! StartupStatus(complete = false)
      }

    case Terminated(corpse) => onTerminated(corpse)
    case ShutdownSystem     => onSystemShutdown()
  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
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
    context.system.stop(graph)
    context.system.stop(cli)
    context.system.stop(parser)
    context.system.stop(index)
    context.system.stop(searcher)
    context.system.stop(updater)

    //cluster.leave(cluster.selfAddress) // leave the cluster before shutdown

    context.system.terminate().onComplete(_ => log.info("system.terminate() finished"))
    //context.stop(self)  // master

  }

}
