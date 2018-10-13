package io.hemin.engine.searcher

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.hemin.engine.EngineProtocol.{ActorRefSupervisor, ReportSearcherStartupComplete}
import io.hemin.engine.domain.ResultsWrapper
import io.hemin.engine.searcher.Searcher.{SearcherRequest, SearcherResults}
import io.hemin.engine.searcher.retriever.SolrRetriever

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Searcher {
  final val name = "searcher"
  def props(config: SearcherConfig): Props =
    Props(new Searcher(config))
      .withDispatcher("hemin.searcher.dispatcher")

  trait SearcherMessage
  trait SearcherQuery extends SearcherMessage
  trait SearcherQueryResult extends SearcherMessage
  // SearchQueries
  final case class SearcherRequest(query: String, page: Option[Int], size: Option[Int]) extends SearcherQuery
  // SearchQueryResults
  final case class SearcherResults(results: ResultsWrapper) extends SearcherQueryResult
}

class Searcher (config: SearcherConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

  private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("hemin.searcher.dispatcher")

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

  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
  }

}
