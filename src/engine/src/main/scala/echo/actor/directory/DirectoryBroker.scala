package echo.actor.directory

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Put, Subscribe, SubscribeAck}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol.ActorRefCrawlerActor
import echo.actor.directory.DirectoryProtocol.{DirectoryCommand, DirectoryEvent, DirectoryQuery}

/**
  * @author Maximilian Irro
  */

object DirectoryBroker {
    final val name = "directory"
    def props(): Props = Props(new DirectoryBroker())
}

class DirectoryBroker extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val STORE_COUNT: Int = Option(CONFIG.getInt("echo.directory.store-count")).getOrElse(1) // TODO
    private val DATABASE_URLs = Array("jdbc:h2:mem:echo1", "jdbc:h2:mem:echo2")// TODO I'll have to thing about a better solution in a distributed context

    private val eventStreamName = Option(CONFIG.getString("echo.directory.event-stream")).getOrElse("directory-event-stream")
    private val mediator = DistributedPubSub(context.system).mediator
    mediator ! Subscribe(eventStreamName, self) // subscribe to the topic (= event stream)
    mediator ! Put(self) // register to the path

    private var crawler: ActorRef = _

    /*
     * We define two separate routings, based on the Command–query separation principle
     * - Command messages (create/update/delete) are PubSub sent to all stores
     * - Query messages (read) are sent to one store
     */
    private var broadcastRouter: Router = _
    private var roundRobinRouter: Router = _ ;
    {
        val routees: Vector[ActorRefRoutee] = (1 to List(STORE_COUNT, DATABASE_URLs.length).min)
            .map(i => {
                val databaseUrl = DATABASE_URLs(i-1)
                val directoryStore = createDirectoryStore(i, databaseUrl)
                context watch directoryStore
                ActorRefRoutee(directoryStore)
            })
            .to[Vector]

        broadcastRouter = Router(BroadcastRoutingLogic(), routees)
        roundRobinRouter = Router(RoundRobinRoutingLogic(), routees)
    }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {

        case SubscribeAck(Subscribe(`eventStreamName`, None, `self`)) =>
            log.info("successfully subscribed to : {}", eventStreamName)

        case msg @ ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref
            broadcastRouter.route(msg, sender())

        case command: DirectoryCommand =>
            log.debug("Routing command: {}", command.getClass)
            roundRobinRouter.route(command, sender())

        case event: DirectoryEvent =>
            log.debug("Routing event: {}", event.getClass)
            broadcastRouter.route(event, sender())

        case query: DirectoryQuery =>
            log.debug("Routing query : {}", query.getClass)
            roundRobinRouter.route(query, sender())

        case Terminated(corpse) =>
            log.warning(s"A ${self.path} store died : {}", corpse.path.name)
            removeStore(corpse)

        case message =>
            log.warning("Routing GENERAL message of kind (assuming it should be broadcast) : {}", message.getClass)
            broadcastRouter.route(message, sender())
    }

    private def createDirectoryStore(storeIndex: Int, databaseUrl: String): ActorRef = {
        val directoryStore = context.actorOf(DirectoryStore.props(databaseUrl),
            name = DirectoryStore.name(storeIndex))

        // forward the actor refs to the worker, but only if those references haven't died
        Option(crawler).foreach(c => directoryStore ! ActorRefCrawlerActor(c) )

        directoryStore
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
