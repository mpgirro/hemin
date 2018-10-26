package io.hemin.engine.index

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.hemin.engine.Node.{ActorRefSupervisor, ReportIndexStoreStartupComplete}
import io.hemin.engine.exception.SearchException
import io.hemin.engine.index.IndexStore._
import io.hemin.engine.index.committer.SolrCommitter
import io.hemin.engine.model.{IndexDoc, ResultPage}
import io.hemin.engine.util.ExecutorServiceWrapper

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
  final case class IndexSearch(query: String, page: Int, size: Int) extends IndexQuery
  // IndexQueryResults
  final case class IndexSearchResults(results: ResultPage) extends IndexQueryResult
}

class IndexStore (config: IndexConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher : {}", self.path.name, context.system.dispatchers.lookup(context.props.dispatcher))
  log.debug("{} running with mailbox : {}", self.path.name, context.system.mailboxes.lookup(context.props.mailbox))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private val solrCommiter: SolrCommitter = new SolrCommitter(config, new ExecutorServiceWrapper())

  /*
  private var indexChanged = false
  private val cache: mutable.Queue[IndexDoc] = new mutable.Queue
  private val updateWebsiteQueue: mutable.Queue[(String,String)] = new mutable.Queue
  private val updateImageQueue: mutable.Queue[(String,String)] = new mutable.Queue
  private val updateLinkQueue: mutable.Queue[(String,String)] = new mutable.Queue
  */

  private var supervisor: ActorRef = _

  override def postRestart(cause: Throwable): Unit = {
    log.warning("{} has been restarted or resumed", self.path.name)
    cause match {
      case e: SearchException =>
        log.error("Error trying to search the index; reason: {}", e.getMessage)
      case e: Exception =>
        log.error("Unhandled Exception : {}", e.getMessage, e)
        sender ! IndexSearchResults(ResultPage()) // TODO besser eine neue antwortmessage a la ErrorIndexResult und entsprechend den fehler in der UI anzeigen zu können
      //currQuery = ""
    }
    super.postRestart(cause)
  }

  override def postStop: Unit = {
    //Option(luceneCommitter).foreach(_.destroy())
    //Option(luceneSearcher).foreach(_.destroy())

    log.info("{} subsystem shutting down", IndexStore.name.toUpperCase)
  }

  override def receive: Receive = {

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      supervisor ! ReportIndexStoreStartupComplete

    case AddDocIndexEvent(doc) =>
      log.debug("Received IndexStoreAddDoc({})", doc.id)
      //cache.enqueue(doc)
      solrCommiter.save(doc)

      /*
    case UpdateDocWebsiteDataIndexEvent(id, html) =>
      log.debug("Received IndexStoreUpdateDocWebsiteData({},_)", id)
      updateWebsiteQueue.enqueue((id,html))

    // TODO this fix is not done in the Directory and only correct data gets send to the index anyway...
    case UpdateDocImageIndexEvent(id, image) =>
      log.debug("Received IndexStoreUpdateDocImage({},{})", id, image)
      updateImageQueue.enqueue((id, image))

    case UpdateDocLinkIndexEvent(id, link) =>
      log.debug("Received IndexStoreUpdateDocLink({},'{}')", id, link)
      updateLinkQueue.enqueue((id, link))
      */

  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
  }

}
