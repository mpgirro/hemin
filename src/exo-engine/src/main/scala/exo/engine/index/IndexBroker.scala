package exo.engine.index

import akka.actor.SupervisorStrategy.{Escalate, Resume}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Put, Subscribe, SubscribeAck}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import exo.engine.EngineProtocol._
import exo.engine.domain.dto.{IndexDoc, ResultWrapper}
import exo.engine.exception.SearchException
import exo.engine.index.IndexStore.{IndexCommand, IndexEvent, IndexQuery}

import scala.collection.JavaConverters._
import scala.concurrent.duration._

/**
  * @author Maximilian Irro
  */
object IndexBroker {
    final val name = "index"
    def props(): Props = Props(new IndexBroker())
}

class IndexBroker extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val STORE_COUNT: Int = Option(CONFIG.getInt("echo.index.store-count")).getOrElse(1) // TODO
    private val INDEX_PATHs = Array("/Users/max/volumes/echo/index_1", "/Users/max/volumes/echo/index_2") // TODO I'll have to thing about a better solution in a distributed context
    private val CREATE_INDEX: Boolean = Option(CONFIG.getBoolean("echo.index.create-index")).getOrElse(false)

    private val eventStreamName = Option(CONFIG.getString("echo.index.event-stream")).getOrElse("index-event-stream")
    private val mediator = DistributedPubSub(context.system).mediator
    mediator ! Subscribe(eventStreamName, self) // subscribe to the topic (= event stream)
    mediator ! Put(self) // register to the path

    /*
     * We define two separate routings, based on the Command–query separation principle
     * - Command messages (create/update/delete) are PubSub sent to all stores
     * - Query messages (read) are sent to one store
     */
    private var broadcastRouter: Router = _ ;
    private var roundRobinRouter: Router = _ ;
    {
        // TODO 1 to List(STORE_COUNT, INDEX_PATHs.length).min
        val routees: Vector[ActorRefRoutee] = (1 to List(STORE_COUNT, INDEX_PATHs.length).min)
            .map(i => {
                val indexPath = INDEX_PATHs(i-1)
                val indexStore = createIndexStore(i, indexPath)
                context watch indexStore
                ActorRefRoutee(indexStore)
            })
            .to[Vector]

        broadcastRouter = Router(BroadcastRoutingLogic(), routees)
        roundRobinRouter = Router(RoundRobinRoutingLogic(), routees)
    }

    // TODO is this working when running in a cluster setup?
    override val supervisorStrategy: SupervisorStrategy =
        OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
            case _: SearchException => Resume
            case _: Exception       => Escalate
        }

    override def receive: Receive = {

        case SubscribeAck(Subscribe(`eventStreamName`, None, `self`)) =>
            log.info("successfully subscribed to : {}", eventStreamName)

        case command: IndexCommand =>
            log.debug("Routing command: {}", command.getClass)
            roundRobinRouter.route(command, sender())

        case event: IndexEvent =>
            log.debug("Routing event: {}", event.getClass)
            broadcastRouter.route(event, sender())

        case query: IndexQuery =>
            log.debug("Routing query : {}", query.getClass)
            roundRobinRouter.route(query, sender())

        case Terminated(corpse) =>
            log.warning(s"A ${self.path} store died : {}", corpse.path.name)
            removeStore(corpse)

        case message =>
            log.warning("Routing GENERAL message of kind (assuming it should be broadcast) : {}", message.getClass)
            broadcastRouter.route(message, sender())

    }

    private def createIndexStore(storeIndex: Int, indexPath: String): ActorRef = {
        val index = context.actorOf(IndexStore.props(indexPath, CREATE_INDEX), IndexStore.name(storeIndex))
        context.watch(index)

        index ! ActorRefSupervisor(self)

        index
    }

    private def removeStore(routee: ActorRef): Unit = {
        broadcastRouter = broadcastRouter.removeRoutee(routee)
        roundRobinRouter = roundRobinRouter.removeRoutee(routee)
        if (broadcastRouter.routees.isEmpty || roundRobinRouter.routees.isEmpty) {
            log.error("Broker shutting down due to no more stores available")
            context.stop(self)
        }
    }
}
