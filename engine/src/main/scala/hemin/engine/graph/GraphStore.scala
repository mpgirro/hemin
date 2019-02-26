package hemin.engine.graph

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.Logger
import hemin.engine.node.Node.{ActorRefSupervisor, ReportGraphStoreInitializationComplete}
import org.neo4j.driver.v1.{AuthTokens, Driver, GraphDatabase}

import scala.concurrent.ExecutionContext

object GraphStore {
  final val name = "graph"

  def props(config: GraphConfig): Props =
    Props(new GraphStore(config))
      .withDispatcher(config.dispatcher)
      .withMailbox(config.mailbox)

  trait GraphStoreMessage

}

class GraphStore (config: GraphConfig)
  extends Actor {

  private val log: Logger = Logger(getClass)

  log.debug("{} running on dispatcher : {}", self.path.name, context.system.dispatchers.lookup(context.props.dispatcher))
  log.debug("{} running with mailbox : {}", self.path.name, context.system.mailboxes.lookup(context.props.mailbox))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private val driver: Driver = GraphDatabase.driver(config.neo4jUri, AuthTokens.basic(config.username, config.password))

  private var supervisor: ActorRef = _

  override def postStop: Unit = {
    log.info("{} subsystem shutting down", GraphStore.name.toUpperCase)
  }

  override def receive: Receive = {

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      supervisor ! ReportGraphStoreInitializationComplete

  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
  }

}
