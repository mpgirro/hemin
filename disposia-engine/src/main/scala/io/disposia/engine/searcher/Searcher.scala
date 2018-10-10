package io.disposia.engine.searcher

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.disposia.engine.EngineProtocol.{ActorRefSupervisor, ReportSearcherStartupComplete}
import io.disposia.engine.index.IndexConfig
import io.disposia.engine.domain.ResultsWrapper
import io.disposia.engine.searcher.Searcher.{SearcherRequest, SearcherResults}
import io.disposia.engine.searcher.retriever.SolrRetriever

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Searcher {
  final val name = "searcher"
  def props(config: IndexConfig): Props =
    Props(new Searcher(config))
      .withDispatcher("echo.searcher.dispatcher")

  trait SearcherMessage
  trait SearcherQuery extends SearcherMessage
  trait SearcherQueryResult extends SearcherMessage
  // SearchQueries
  case class SearcherRequest(query: String, page: Int, size: Int) extends SearcherQuery
  // SearchQueryResults
  case class SearcherResults(results: ResultsWrapper) extends SearcherQueryResult
}

class Searcher (config: IndexConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

  private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.searcher.dispatcher")

  private val solrRetriever = new SolrRetriever(config, executionContext)

  private var supervisor: ActorRef = _

  override def postStop: Unit = {

    log.info("shutting down")
  }

  override def receive: Receive = {

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      supervisor ! ReportSearcherStartupComplete

    case SearcherRequest(query, page, size) =>
      log.debug("Received SearchRequest('{}',{},{}) message", query, page, size)

      val theSender = sender()
      solrRetriever.search(query, page, size)
        .onComplete {
          case Success(rs) => theSender ! SearcherResults(rs)
          case Failure(ex) =>
            log.error("Error on searching Index : {}", ex)
            ex.printStackTrace()
        }

    case unhandled => log.warning("Received unhandled message of type : {}", unhandled.getClass)
  }

}
