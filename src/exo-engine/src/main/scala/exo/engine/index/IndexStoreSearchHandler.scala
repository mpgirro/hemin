package exo.engine.index

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import exo.engine.EngineProtocol._
import exo.engine.domain.dto.ResultWrapper
import exo.engine.index.IndexProtocol.{IndexResultsFound, SearchIndex}
import exo.engine.index.IndexStoreSearchHandler.RefreshIndexSearcher
import exo.engine.exception.SearchException

import scala.concurrent.blocking


/**
  * @author Maximilian Irro
  */

object IndexStoreSearchHandler {

    case object RefreshIndexSearcher

    def name(handlerIndex: Int): String = "handler-" + handlerIndex
    def props(indexSearcher: IndexSearcher): Props = {
        Props(new IndexStoreSearchHandler(indexSearcher))
            .withDispatcher("echo.index.dispatcher")
    }
}

class IndexStoreSearchHandler(indexSearcher: IndexSearcher) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private var currQuery: String = ""

    private var supervisor: ActorRef = _

    override def postRestart(cause: Throwable): Unit = {
        log.info("{} has been restarted or resumed", self.path.name)
        cause match {
            case e: SearchException =>
                log.error("Error trying to search the index; reason: {}", e.getMessage)
            case e: Exception =>
                log.error("Unhandled Exception : {}", e.getMessage, e)
                sender ! IndexProtocol.IndexResultsFound(currQuery, ResultWrapper.empty()) // TODO besser eine neue antwortmessage a la ErrorIndexResult und entsprechend den fehler in der UI anzeigen zu können
                currQuery = ""
        }
        super.postRestart(cause)
    }

    override def postStop: Unit = {
        Option(indexSearcher).foreach(_.destroy())
        log.info("shutting down")
    }


    override def receive: Receive = {

        case ActorRefSupervisor(ref) =>
            log.debug("Received ActorRefSupervisor(_)")
            supervisor = ref

        case RefreshIndexSearcher =>
            log.debug("Received RefreshIndexSearcher(_)")
            blocking {
                indexSearcher.refresh()
            }

        case SearchIndex(query, page, size) =>
            log.debug("Received SearchIndex('{}',{},{}) message", query, page, size)

            currQuery = query // make a copy in case of an exception
            var results: ResultWrapper = null
            blocking {
                results = indexSearcher.search(query, page, size)
            }

            if(results.getTotalHits > 0){
                sender ! IndexResultsFound(query,results)
            } else {
                log.warning("No Podcast matching query: '{}' found in the index", query)
                //sender ! NoIndexResultsFound(query)
                sender ! IndexResultsFound(query,ResultWrapper.empty())
            }

            currQuery = "" // wipe the copy
    }

}
