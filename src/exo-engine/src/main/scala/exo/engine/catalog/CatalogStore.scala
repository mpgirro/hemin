package exo.engine.catalog

import java.sql.{Connection, DriverManager}

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import exo.engine.EngineProtocol._
import liquibase.{Contexts, LabelExpression, Liquibase}
import liquibase.database.{Database, DatabaseFactory}
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

import scala.collection.JavaConverters._

/**
  * @author Maximilian Irro
  */

object CatalogStore {
    def name(storeIndex: Int): String = "store-" + storeIndex
    def props(databaseUrl: String): Props = {
        Props(new CatalogStore(databaseUrl)).withDispatcher("echo.catalog.dispatcher")
    }
}

class CatalogStore(databaseUrl: String) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val WORKER_COUNT: Int = Option(CONFIG.getInt("echo.catalog.worker-count")).getOrElse(5)

    private var currentWorkerIndex = 0

    private var crawler: ActorRef = _
    private var updater: ActorRef = _
    private var supervisor: ActorRef = _

    private var router: Router = {
        val routees = Vector.fill(WORKER_COUNT) {
            val catalogStore = createCatalogStoreWorkerActor(databaseUrl)
            context watch catalogStore
            ActorRefRoutee(catalogStore)
        }
        Router(RoundRobinRoutingLogic(), routees)
    }

    override def preStart(): Unit = {
        runLiquibaseUpdate()
    }

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {

        case msg @ ActorRefCrawlerActor(ref) =>
            log.debug("Received ActorRefCrawlerActor(_)")
            crawler = ref
            router.routees.foreach(r => r.send(msg, sender()))

        case msg @ ActorRefUpdaterActor(ref) =>
            log.debug("Received ActorRefUpdaterActor(_)")
            updater = ref
            router.routees.foreach(r => r.send(msg, sender()))

        case ActorRefSupervisor(ref) =>
            log.debug("Received ActorRefSupervisor(_)")
            supervisor = ref

        case Terminated(corpse) =>
            log.error(s"A ${self.path} worker died : {}", corpse.path.name)
            context.stop(self)

        case PoisonPill =>
            log.debug("Received a PosionPill -> forwarding it to all routees")
            router.routees.foreach(r => r.send(PoisonPill, sender()))

        case work =>
            log.debug("Routing work of kind : {}", work.getClass)
            router.route(work, sender())

    }

    private def createCatalogStoreWorkerActor(databaseUrl: String): ActorRef = {
        currentWorkerIndex += 1
        val workerIndex = currentWorkerIndex
        val catalogStore = context.actorOf(CatalogStoreHandler.props(workerIndex, databaseUrl), CatalogStoreHandler.name(workerIndex))

        // forward the actor refs to the worker, but only if those references haven't died
        Option(crawler).foreach(c => catalogStore ! ActorRefCrawlerActor(c) )
        catalogStore ! ActorRefSupervisor(self)

        catalogStore
    }

    private def runLiquibaseUpdate(): Unit = {
        val startTime = System.currentTimeMillis
        try {
            Class.forName("org.h2.Driver")
            val conn: Connection = DriverManager.getConnection(
                s"${databaseUrl};DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                "sa",
                "")

            val database: Database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn))
            //database.setDefaultSchemaName("echo")

            val liquibase: Liquibase = new Liquibase("liquibase/master.xml", new ClassLoaderResourceAccessor(), database)

            val isDropFirst = true // TODO set this as a parameter
            if (isDropFirst) {
                liquibase.dropAll()
            }

            if(liquibase.isSafeToRunUpdate){
                liquibase.update(new Contexts(), new LabelExpression())
            } else {
                log.warning("Liquibase reports it is NOT safe to run the update")
            }
        } catch {
            case e: Exception =>
                log.error("Error on Liquibase update: {}", e)
        } finally {
            val stopTime = System.currentTimeMillis
            val elapsedTime = stopTime - startTime
            log.info("Run Liquibase in {} ms", elapsedTime)
        }
    }
}
