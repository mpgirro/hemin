package hemin.engine.searcher

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.Logger
import hemin.engine.model.SearchResult
import hemin.engine.node.Node.{ActorRefSupervisor, ReportSearcherStartupComplete}
import hemin.engine.searcher.Searcher.{SearchRequest, SearchResults}
import hemin.engine.searcher.retriever.{IndexRetriever, SolrRetriever}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Searcher {
  final val name = "searcher"
  def props(config: SearcherConfig): Props =
    Props(new Searcher(config))
      .withDispatcher(config.dispatcher)
      .withMailbox(config.mailbox)

  trait SearcherMessage
  trait SearcherQuery extends SearcherMessage
  trait SearcherQueryResult extends SearcherMessage
  // SearchQueries
  final case class SearchRequest(query: String, pageNumber: Option[Int], pageSize: Option[Int]) extends SearcherQuery
  // SearchQueryResults
  final case class SearchResults(results: SearchResult) extends SearcherQueryResult
}

class Searcher (config: SearcherConfig)
  extends Actor {

  private val log: Logger = Logger(getClass)

  log.debug("{} running on dispatcher : {}", self.path.name, context.system.dispatchers.lookup(context.props.dispatcher))
  log.debug("{} running with mailbox : {}", self.path.name, context.system.mailboxes.lookup(context.props.mailbox))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private val retriever: IndexRetriever = new SolrRetriever(config, executionContext)

  private var supervisor: ActorRef = _

  override def postStop: Unit = {
    log.info("{} subsystem shutting down", Searcher.name.toUpperCase)
  }

  override def receive: Receive = {

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      supervisor ! ReportSearcherStartupComplete

    case SearchRequest(query, page, size) =>
      log.debug("Received SearchRequest('{}',{},{}) message", query, page, size)

      val theSender = sender()
      retriever.search(query, page, size)
        .onComplete {
          case Success(rs) => theSender ! SearchResults(rs)
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