package io.hemin.engine.searcher

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.Logger
import io.hemin.engine.model.SearchResult
import io.hemin.engine.node.Node.{ActorRefSupervisor, ReportSearcherInitializationComplete}
import io.hemin.engine.searcher.Searcher.{SearchRequest, SearchReply}
import io.hemin.engine.searcher.retriever.{IndexRetriever, SolrRetriever}

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
  final case class SearchReply(request: SearchRequest, result: SearchResult) extends SearcherQueryResult
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
      supervisor ! ReportSearcherInitializationComplete

    case request@SearchRequest(query, pageNumber, pageSize) =>
      log.debug("Received SearchRequest('{}',{},{}) message", query, pageNumber, pageSize)

      val theSender = sender()
      retriever.search(query, pageNumber, pageSize)
        .onComplete {
          case Success(result) => theSender ! SearchReply(request, result)
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
