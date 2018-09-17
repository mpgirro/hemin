package exo.engine

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import exo.engine.EngineProtocol.SearchResults
import exo.engine.NodeMaster.{GetCatalogBroker, GetIndexBroker, GetUpdater}
import exo.engine.catalog.CatalogProtocol.ProposeNewFeed
import exo.engine.domain.dto.ResultWrapperDTO
import exo.engine.index.IndexProtocol.{IndexResultsFound, SearchIndex}

import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  * @author max
  */
class ExoEngine {

    private val CONFIG = ConfigFactory.load()
    private implicit val INTERNAL_TIMEOUT: Timeout = Option(CONFIG.getInt("echo.internal-timeout")).getOrElse(5).seconds
    private val DEFAULT_PAGE: Int = Option(CONFIG.getInt("echo.gateway.default-page")).getOrElse(1)
    private val DEFAULT_SIZE: Int = Option(CONFIG.getInt("echo.gateway.default-size")).getOrElse(20)

    private val BREAKER_CALL_TIMEOUT: FiniteDuration = Option(CONFIG.getInt("echo.gateway.breaker-call-timeout")).getOrElse(5).seconds
    private val BREAKER_RESET_TIMEOUT: FiniteDuration = Option(CONFIG.getInt("echo.gateway.breaker-reset-timeout")).getOrElse(10).seconds
    private val MAX_BREAKER_FAILURES: Int = 2 // TODO read from config

    private val log = Logger(classOf[ExoEngine])

    private var master: ActorRef = _

    private implicit val ec: ExecutionContext = ExecutionContext.global // TODO anderen als global EC

    /* TODO ich will einen CircuitBreaker, habe aber keinen Scheduler weil das hier kein Actor ist
    private val indexBreaker =
        CircuitBreaker(context.system.scheduler, MAX_BREAKER_FAILURES, BREAKER_CALL_TIMEOUT, BREAKER_RESET_TIMEOUT)
            .onOpen(breakerOpen("Index"))
            .onClose(breakerClose("Index"))
            .onHalfOpen(breakerHalfOpen("Index"))
    */

    def start(): Unit = {
        val config = ConfigFactory.load
        val system = ActorSystem("exo", config)
        master = system.actorOf(Props(new NodeMaster), NodeMaster.name)
    }

    def bus(): ActorRef = master

    def catalogBroker(): ActorRef = {
        val future = master ? GetCatalogBroker
        val catalog = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[ActorRef]
        catalog
    }

    def indexBroker(): ActorRef = {
        val future = master ? GetIndexBroker
        val index = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[ActorRef]
        index
    }

    def propose(url: String): Unit = {
        val future = master ? GetUpdater
        val updater = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[ActorRef]
        updater ! ProposeNewFeed(url)
    }

    def search(query: String, page: Int, size: Int): Future[ResultWrapperDTO] = {
        val p = Promise[ResultWrapperDTO]()
        val f = p.future
        Future {
            val index = indexBroker() // TODO hier nich textra die lookup methode aufrufen!
            (index ? SearchIndex(query, page, size)).onComplete{
                case Success(result) => result match {
                    case IndexResultsFound(_,r) => p success r
                }
                case fail @ Failure (_) => fail
                    /*
                    log.error(s"Search for '$query' failed ; reason : ${reason.getMessage}")
                    p success ResultWrapperDTO.empty()
                    */
            }
        }
        p.future // immediatelly return the promise's future as a placeholder
    }

    def search2(query: String, page: Int, size: Int): Future[ResultWrapperDTO] = {
        (indexBroker() ? SearchIndex(query, page, size)).map(m => {
            m match {
                case IndexResultsFound(_,results) => results
            }
        })
    }

    private def breakerOpen(name: String): Unit = {
        log.warn("{} Circuit Breaker is open", name)
    }

    private def breakerClose(name: String): Unit = {
        log.warn("{} Circuit Breaker is closed", name)
    }

    private def breakerHalfOpen(name: String): Unit = {
        log.warn("{} Circuit Breaker is half-open, next message goes through", name)
    }

}
