package io.hemin.engine.index

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.hemin.engine.EngineProtocol._
import io.hemin.engine.domain.{IndexDoc, ResultsWrapper}
import io.hemin.engine.index.IndexStore._
import io.hemin.engine.index.committer.SolrCommitter
import io.hemin.engine.util.ExecutorServiceWrapper
import io.hemin.engine.exception.SearchException

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

object IndexStore {
  final val name = "index"
  def props(config: IndexConfig): Props =
    Props(new IndexStore(config))
      .withDispatcher("hemin.index.dispatcher")

  trait IndexMessage
  trait IndexEvent extends IndexMessage
  trait IndexCommand extends IndexMessage
  trait IndexQuery extends IndexMessage
  trait IndexQueryResult extends IndexMessage
  // IndexEvents
  case class AddDocIndexEvent(doc: IndexDoc) extends IndexEvent
  case class UpdateDocWebsiteDataIndexEvent(id: String, html: String) extends IndexEvent
  case class UpdateDocImageIndexEvent(id: String, image: String) extends IndexEvent
  case class UpdateDocLinkIndexEvent(id: String, newLink: String) extends IndexEvent
  // IndexCommands
  //case class CommitIndex() extends IndexCommand
  // IndexQueries
  case class IndexSearch(query: String, page: Int, size: Int) extends IndexQuery
  // IndexQueryResults
  case class IndexSearchResults(results: ResultsWrapper) extends IndexQueryResult
}

class IndexStore (config: IndexConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

  private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("hemin.index.dispatcher")

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
        sender ! IndexSearchResults(ResultsWrapper()) // TODO besser eine neue antwortmessage a la ErrorIndexResult und entsprechend den fehler in der UI anzeigen zu können
      //currQuery = ""
    }
    super.postRestart(cause)
  }

  override def postStop: Unit = {
    //Option(luceneCommitter).foreach(_.destroy())
    //Option(luceneSearcher).foreach(_.destroy())

    log.info("shutting down")
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

    case unhandled => log.warning("Received unhandled message of type : {}", unhandled.getClass)

  }

}
