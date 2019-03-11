package hemin.engine.index

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.Logger
import hemin.engine.index.IndexStore._
import hemin.engine.index.committer.SolrCommitter
import hemin.engine.model.{IndexDoc, SearchResult}
import hemin.engine.node.Node.{ActorRefSupervisor, ReportIndexStoreInitializationComplete}
import hemin.engine.util.ExecutorServiceWrapper

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

object IndexStore {
  final val name = "index"
  def props(config: IndexConfig): Props =
    Props(new IndexStore(config))
      .withDispatcher(config.dispatcher)
      .withMailbox(config.mailbox)

  trait IndexMessage
  trait IndexEvent extends IndexMessage
  trait IndexCommand extends IndexMessage
  trait IndexQuery extends IndexMessage
  trait IndexQueryResult extends IndexMessage
  // IndexEvents
  final case class AddDocIndexEvent(doc: IndexDoc) extends IndexEvent
  final case class UpdateDocWebsiteDataIndexEvent(id: String, html: String) extends IndexEvent
  final case class UpdateDocImageIndexEvent(id: String, image: String) extends IndexEvent
  final case class UpdateDocLinkIndexEvent(id: String, newLink: String) extends IndexEvent
  // IndexCommands
  //case class CommitIndex() extends IndexCommand
  // IndexQueries
  final case class IndexSearch(query: String, pageNumber: Int, pageSize: Int) extends IndexQuery
  // IndexQueryResults
  final case class IndexSearchResults(results: SearchResult) extends IndexQueryResult
}

class IndexStore (config: IndexConfig)
  extends Actor {

  private val log: Logger = Logger(getClass)

  log.debug("{} running on dispatcher : {}", self.path.name, context.system.dispatchers.lookup(context.props.dispatcher))
  log.debug("{} running with mailbox : {}", self.path.name, context.system.mailboxes.lookup(context.props.mailbox))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private val commiter: SolrCommitter = new SolrCommitter(config, new ExecutorServiceWrapper())

  private var supervisor: ActorRef = _

  if (config.createIndex) {
    log.info("Deleting all Index documents on startup")
    commiter.deleteAll()
  }

  override def postRestart(cause: Throwable): Unit = {
    log.warn("{} has been restarted or resumed", self.path.name)
    cause match {
      case e: Exception =>
        log.error("Unhandled Exception : {}", e.getMessage, e)
        sender ! IndexSearchResults(SearchResult()) // TODO besser eine neue antwortmessage a la ErrorIndexResult und entsprechend den fehler in der UI anzeigen zu können
      //currQuery = ""
    }
    super.postRestart(cause)
  }

  override def postStop: Unit = {
    log.info("{} subsystem shutting down", IndexStore.name.toUpperCase)
  }

  override def receive: Receive = {

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      supervisor ! ReportIndexStoreInitializationComplete

    case AddDocIndexEvent(doc) =>
      log.debug("Received IndexStoreAddDoc({})", doc.id)
      commiter.save(doc)

  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
  }

}
