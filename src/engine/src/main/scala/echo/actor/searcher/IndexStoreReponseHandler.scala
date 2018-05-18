package echo.actor.searcher

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.event.LoggingReceive
import akka.util.Timeout
import echo.actor.ActorProtocol.SearchResults
import echo.actor.index.IndexProtocol.{IndexResultsFound, NoIndexResultsFound}
import echo.actor.searcher.IndexStoreReponseHandler.IndexRetrievalTimeout
import echo.core.domain.dto.{ModifiableIndexDocDTO, ResultWrapperDTO}
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.language.postfixOps


/**
  * @author Maximilian Irro
  */

object IndexStoreReponseHandler {

    case object IndexRetrievalTimeout

    def props(indexStore: ActorRef, originalSender: Option[ActorRef], internalTimeout: FiniteDuration): Props = {
        Props(new IndexStoreReponseHandler(indexStore, originalSender.get, internalTimeout))
            .withDispatcher("echo.searcher.dispatcher")
    }
}

class IndexStoreReponseHandler(indexStore: ActorRef,
                               originalSender: ActorRef,
                               internalTimeout: FiniteDuration) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    override def receive: Receive = {

        case IndexResultsFound(query: String, resultWrapper: ResultWrapperDTO) =>
            log.info("Received {} results from index for query '{}'", resultWrapper.getTotalHits, query)
            timeoutMessager.cancel

            // TODO remove <img> tags from description, I suspect it to cause troubles
            // TODO this should probably better done while indexing! (so only has to be done one time instead of every retrieval
            resultWrapper.getResults
                .asScala
                .map(r => new ModifiableIndexDocDTO().from(r))
                .map(r => {
                    Option(r.getDescription).map(value => {
                        val soupDoc = Jsoup.parse(value)
                        soupDoc.select("img").remove
                        // if not removed, the cleaner will drop the <div> but leave the inner text
                        val clean = Jsoup.clean(soupDoc.body.html, Whitelist.basic)
                        r.setDescription(clean)
                    })
                    r
                })
                .map(r => r.toImmutable)

            sendResponseAndShutdown(SearchResults(resultWrapper))

        case NoIndexResultsFound(query: String) =>
            log.info("Received NO results from index for query '{}'", query)
            timeoutMessager.cancel
            sendResponseAndShutdown(SearchResults(ResultWrapperDTO.empty()))

        case IndexRetrievalTimeout =>
            log.warning("IndexRetrievalTimeout triggered")
            sendResponseAndShutdown(IndexRetrievalTimeout)

        case _ =>
            log.debug("Stopping because received an unknown message : {}", self.path.name)
            context.stop(self)
    }

    def sendResponseAndShutdown(response: Any): Unit = {
        originalSender ! response
        log.debug("Stopping : {}", self.path.name)
        context.stop(self)
    }

    import context.dispatcher
    val timeoutMessager: Cancellable = context.system.scheduler.
        scheduleOnce(internalTimeout) { // TODO read timeout val from config
            self ! IndexRetrievalTimeout
        }
}
