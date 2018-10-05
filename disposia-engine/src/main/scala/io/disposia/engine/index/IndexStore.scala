package io.disposia.engine.index

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.disposia.engine.EngineProtocol._
import io.disposia.engine.olddomain._
import io.disposia.engine.exception.SearchException
import io.disposia.engine.index.IndexStore._
import io.disposia.engine.index.committer.SolrCommitter
import io.disposia.engine.domain.{IndexDoc, Results}
import io.disposia.engine.util.ExecutorServiceWrapper
import io.disposia.engine.util.mapper.IndexMapper

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.compat.java8.OptionConverters._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future, blocking}
import scala.language.postfixOps

object IndexStore {
  final val name = "index"
  def props(config: IndexConfig): Props =
    Props(new IndexStore(config)).withDispatcher("echo.index.dispatcher")

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
  case class CommitIndex() extends IndexCommand
  // IndexQueries
  case class IndexSearch(query: String, page: Int, size: Int) extends IndexQuery
  // IndexQueryResults
  case class IndexSearchResults(results: Results) extends IndexQueryResult
}

class IndexStore (config: IndexConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

  private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.index.dispatcher")

  private val luceneCommitter: IndexCommitter = new LuceneCommitter(config.luceneIndexPath, config.createIndex) // TODO do not alway re-create the index
  private val luceneSearcher: IndexSearcher = new LuceneSearcher(luceneCommitter.asInstanceOf[LuceneCommitter].getIndexWriter)
  private val solrCommiter: SolrCommitter = new SolrCommitter(config, new ExecutorServiceWrapper())

  private var indexChanged = false
  private val cache: mutable.Queue[IndexDoc] = new mutable.Queue
  private val updateWebsiteQueue: mutable.Queue[(String,String)] = new mutable.Queue
  private val updateImageQueue: mutable.Queue[(String,String)] = new mutable.Queue
  private val updateLinkQueue: mutable.Queue[(String,String)] = new mutable.Queue

  private var supervisor: ActorRef = _

  // kickoff the committing play
  context.system.scheduler.schedule(config.commitInterval, config.commitInterval, self, CommitIndex)

  override def postRestart(cause: Throwable): Unit = {
    log.warning("{} has been restarted or resumed", self.path.name)
    cause match {
      case e: SearchException =>
        log.error("Error trying to search the index; reason: {}", e.getMessage)
      case e: Exception =>
        log.error("Unhandled Exception : {}", e.getMessage, e)
        sender ! IndexSearchResults(Results()) // TODO besser eine neue antwortmessage a la ErrorIndexResult und entsprechend den fehler in der UI anzeigen zu können
      //currQuery = ""
    }
    super.postRestart(cause)
  }

  override def postStop: Unit = {
    Option(luceneCommitter).foreach(_.destroy())
    Option(luceneSearcher).foreach(_.destroy())

    log.info("shutting down")
  }

  override def receive: Receive = {

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      supervisor ! ReportIndexStoreStartupComplete

    case CommitIndex =>
      commitIndexIfChanged()

    case AddDocIndexEvent(doc) =>
      log.debug("Received IndexStoreAddDoc({})", doc.id)
      cache.enqueue(doc)
      solrCommiter.save(doc)

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

    case IndexSearch(query, page, size) =>
      log.debug("Received SearchIndex('{}',{},{}) message", query, page, size)

      //var currQuery = query // make a copy in case of an exception
      val origSender = sender()

      Future {
        var results: Results = null
        blocking {
          val rs = luceneSearcher.search(query, page, size)
          //results = luceneSearcher.search(query, page, size)
          results = IndexMapper.toResults(rs)
        }

        if (results.totalHits > 0){
          origSender ! IndexSearchResults(results)
        } else {
          log.warning("No Podcast matching query: '{}' found in the index", query)
          //sender ! NoIndexResultsFound(query)
          origSender ! IndexSearchResults(Results())
        }

        //currQuery = "" // wipe the copy
      }

    case unhandled => log.warning("Received unhandled message of type : {}", unhandled.getClass)

  }

  private def commitIndexIfChanged(): Unit = {
    var committed = false
    //if (indexChanged) {
    if (cache.nonEmpty) {
      log.debug("Committing Index due to pending changes")

      for (doc <- cache) {
        luceneCommitter.add(IndexMapper.toIndexDoc(doc))
      }
      luceneCommitter.commit()
      //indexChanged = false
      cache.clear()

      committed = true
      log.debug("Finished Index due to pending changes")
    }

    if (updateWebsiteQueue.nonEmpty) {
      log.debug("Processing pending entries in website queue")
      luceneSearcher.refresh()
      processWebsiteQueue(updateWebsiteQueue)
      luceneCommitter.commit()
      committed = true
      log.debug("Finished pending entries in website queue")
    }

    if (updateImageQueue.nonEmpty) {
      log.debug("Processing pending entries in image queue")
      luceneSearcher.refresh()
      processImageQueue(updateImageQueue)
      luceneCommitter.commit()
      committed = true
      log.debug("Finished pending entries in image queue")
    }

    if (updateLinkQueue.nonEmpty) {
      log.debug("Processing pending entries in link queue")
      luceneSearcher.refresh()
      processLinkQueue(updateLinkQueue)
      luceneCommitter.commit()
      committed = true
      log.debug("Finished pending entries in link queue")
    }

    if (committed) {
      luceneSearcher.refresh()
    }
  }

  private def processWebsiteQueue(queue: mutable.Queue[(String,String)]): Unit = {
    if (queue.nonEmpty) {
      val (id,html) = queue.dequeue()
      val entry = luceneSearcher.findById(id).asScala.map(_.asInstanceOf[ImmutableOldIndexDoc])
      entry match {
        case Some(doc) => luceneCommitter.update(doc.withWebsiteData(html))
        case None      => log.error("Could not retrieve from index for update website (ID) : {}", id)
      }

      processWebsiteQueue(queue)
    }
  }

  private def processImageQueue(queue: mutable.Queue[(String,String)]): Unit = {
    if (queue.nonEmpty) {
      val (id,image) = queue.dequeue()
      val entry = luceneSearcher.findById(id).asScala.map(_.asInstanceOf[ImmutableOldIndexDoc])
      entry match {
        case Some(doc) => luceneCommitter.update(doc.withImage(image))
        case None      => log.error("Could not retrieve from index for update image (ID) : {}", id)
      }

      processImageQueue(queue)
    }
  }

  private def processLinkQueue(queue: mutable.Queue[(String,String)]): Unit = {
    if (queue.nonEmpty) {
      val (id,link) = queue.dequeue()
      val entry = luceneSearcher.findById(id).asScala.map(_.asInstanceOf[ImmutableOldIndexDoc])
      entry match {
        case Some(doc) => luceneCommitter.update(doc.withLink(link))
        case None      => log.error("Could not retrieve from index for update link (ID) : {}", id)
      }

      processLinkQueue(queue)
    }
  }

}
