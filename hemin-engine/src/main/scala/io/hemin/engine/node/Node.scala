package io.hemin.engine.node

import akka.actor.{Actor, ActorRef, Props, SupervisorStrategy, Terminated}
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import io.hemin.engine.EngineConfig
import io.hemin.engine.catalog.CatalogStore
import io.hemin.engine.catalog.CatalogStore.CatalogMessage
import io.hemin.engine.crawler.Crawler
import io.hemin.engine.crawler.Crawler.CrawlerMessage
import io.hemin.engine.index.IndexStore
import io.hemin.engine.index.IndexStore.IndexMessage
import io.hemin.engine.node.Node._
import io.hemin.engine.parser.Parser
import io.hemin.engine.parser.Parser.ParserMessage
import io.hemin.engine.searcher.Searcher
import io.hemin.engine.searcher.Searcher.SearcherMessage
import io.hemin.engine.updater.Updater
import io.hemin.engine.updater.Updater.UpdaterMessage
import io.hemin.engine.util.InitializationProgress
import io.hemin.engine.util.cli.CliProcessor

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

object Node {
  final val name = "node"
  def props(config: EngineConfig): Props =
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
  final case class ReportCatalogStoreStartupComplete()
  final case class ReportIndexStoreStartupComplete()
  final case class ReportCrawlerStartupComplete()
  final case class ReportParserStartupComplete()
  final case class ReportSearcherStartupComplete()
  final case class ReportUpdaterStartupComplete()
  final case class ReportWorkerStartupComplete() // for worker/handler delegation children

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

class Node(config: EngineConfig)
  extends Actor {

  private val log: Logger = Logger(getClass)

  log.debug("{} running on dispatcher : {}", self.path.name, context.system.dispatchers.lookup(context.props.dispatcher))
  log.debug("{} running with mailbox : {}", self.path.name, context.system.mailboxes.lookup(context.props.mailbox))

  override val supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

  private implicit val executionContext: ExecutionContext = context.dispatcher
  private implicit val internalTimeout: Timeout = config.node.internalTimeout

  //private val cluster = Cluster(context.system)

  private val processor = new CliProcessor(self, config, executionContext)

  private val initializationProgress =
    new InitializationProgress(Seq(CatalogStore.name, Crawler.name, IndexStore.name, Parser.name, Searcher.name, Updater.name))

  private var index: ActorRef = _
  private var catalog: ActorRef = _
  private var crawler: ActorRef = _
  private var parser: ActorRef = _
  private var searcher: ActorRef = _
  private var updater: ActorRef = _

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

    index    = context.watch(context.actorOf(IndexStore.props(config.index),     IndexStore.name))
    parser   = context.watch(context.actorOf(Parser.props(config.parser),        Parser.name))
    crawler  = context.watch(context.actorOf(Crawler.props(config.crawler),      Crawler.name))
    catalog  = context.watch(context.actorOf(CatalogStore.props(config.catalog), CatalogStore.name))
    searcher = context.watch(context.actorOf(Searcher.props(config.searcher),    Searcher.name))
    updater  = context.watch(context.actorOf(Updater.props(config.updater),      Updater.name))


    // pass around references not provided by constructors due to circular dependencies
    index ! ActorRefSupervisor(self)

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
  }

  override def postStop: Unit = {
    log.info("{} shutting down", Node.name.toUpperCase)
  }

  override def receive: Receive = {

    case CliInput(input) => onCliInput(input, sender)

    case msg: CatalogMessage  => catalog.tell(msg, sender())
    case msg: CrawlerMessage  => crawler.tell(msg, sender())
    case msg: IndexMessage    => index.tell(msg, sender())
    case msg: ParserMessage   => parser.tell(msg, sender())
    case msg: SearcherMessage => searcher.tell(msg, sender())
    case msg: UpdaterMessage  => updater.tell(msg, sender())

    case ReportCatalogStoreStartupComplete => initializationProgress.complete(CatalogStore.name)
    case ReportCrawlerStartupComplete      => initializationProgress.complete(Crawler.name)
    case ReportIndexStoreStartupComplete   => initializationProgress.complete(IndexStore.name)
    case ReportParserStartupComplete       => initializationProgress.complete(Parser.name)
    case ReportSearcherStartupComplete     => initializationProgress.complete(Searcher.name)
    case ReportUpdaterStartupComplete      => initializationProgress.complete(Updater.name)

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

  private def onCliInput(input: String, theSender: ActorRef): Unit = processor
    .eval(input)
    .mapTo[String]
    .foreach(output => theSender ! CliOutput(output))

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
