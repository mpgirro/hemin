package exo.engine.catalog

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Put, Subscribe, SubscribeAck}
import akka.routing.{ActorRefRoutee, BroadcastRoutingLogic, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import exo.engine.EngineProtocol._
import exo.engine.catalog.CatalogStore.{CatalogCommand, CatalogEvent, CatalogQuery}
import exo.engine.config.CatalogConfig

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */

object CatalogBroker {
    final val name = "catalog"
    def props(config: CatalogConfig): Props = Props(new CatalogBroker(config))
}

@Deprecated
class CatalogBroker (config: CatalogConfig) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val STORE_COUNT: Int = Option(CONFIG.getInt("echo.catalog.store-count")).getOrElse(1) // TODO
    private val DATABASE_URLs = Array("jdbc:h2:mem:echo1", "jdbc:h2:mem:echo2")// TODO I'll have to thing about a better solution in a distributed context

    private val eventStreamName = Option(CONFIG.getString("echo.catalog.event-stream")).getOrElse("catalog-event-stream")
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
                val catalogStore = createCatalogStore(i, databaseUrl)
                context watch catalogStore
                ActorRefRoutee(catalogStore)
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

        case command: CatalogCommand =>
            log.debug("Routing command: {}", command.getClass)
            roundRobinRouter.route(command, sender())

        case event: CatalogEvent =>
            log.debug("Routing event: {}", event.getClass)
            broadcastRouter.route(event, sender())

        case query: CatalogQuery =>
            log.debug("Routing query : {}", query.getClass)
            roundRobinRouter.route(query, sender())

        case Terminated(corpse) =>
            log.warning(s"A ${self.path} store died : {}", corpse.path.name)
            removeStore(corpse)

        case message =>
            log.warning("Routing GENERAL message of kind (assuming it should be broadcast) : {}", message.getClass)
            broadcastRouter.route(message, sender())
    }

    private def createCatalogStore(storeIndex: Int, databaseUrl: String): ActorRef = {
        val catalogStore = context.actorOf(CatalogStore.props(config),
            name = CatalogStore.name)

        // forward the actor refs to the worker, but only if those references haven't died
        Option(crawler).foreach(c => catalogStore ! ActorRefCrawlerActor(c) )
        catalogStore ! ActorRefSupervisor(self)

        catalogStore
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
