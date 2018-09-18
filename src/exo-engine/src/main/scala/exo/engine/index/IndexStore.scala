package exo.engine.index

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import com.typesafe.config.ConfigFactory
import exo.engine.EngineProtocol._
import exo.engine.config.IndexConfig
import exo.engine.domain.dto._
import exo.engine.exception.SearchException
import exo.engine.index.IndexStore._

import scala.collection.mutable
import scala.compat.java8.OptionConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */
object IndexStore {
    //def name(storeIndex: Int): String = "store-" + storeIndex
    final val name = "index"
    def props(config: IndexConfig): Props = {
        Props(new IndexStore(config)).withDispatcher("echo.index.dispatcher")
    }

    trait IndexMessage
    trait IndexEvent extends IndexMessage
    trait IndexCommand extends IndexMessage
    trait IndexQuery extends IndexMessage
    trait IndexQueryResult extends IndexMessage
    // IndexEvents
    case class AddDocIndexEvent(doc: IndexDoc) extends IndexEvent
    case class UpdateDocWebsiteDataIndexEvent(exo: String, html: String) extends IndexEvent
    case class UpdateDocImageIndexEvent(exo: String, image: String) extends IndexEvent
    case class UpdateDocLinkIndexEvent(exo: String, newLink: String) extends IndexEvent
    // IndexCommands
    case class CommitIndex() extends IndexCommand
    // IndexQueries
    case class SearchIndex(query: String, page: Int, size: Int) extends IndexQuery
    // IndexQueryResults
    case class SearchResults(query: String, results: ResultWrapper) extends IndexQueryResult
}

class IndexStore (config: IndexConfig) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    /*
    private val CONFIG = ConfigFactory.load()
    private val COMMIT_INTERVAL: FiniteDuration = Option(CONFIG.getInt("echo.index.commit-interval")).getOrElse(3).seconds
    private val WORKER_COUNT: Int = Option(CONFIG.getInt("echo.index.handler-count")).getOrElse(5)
    */
    //private var handlerIndex = 0

    //private val mediator = DistributedPubSub(context.system).mediator

    private val indexCommitter: IndexCommitter = new LuceneCommitter(config.indexPath, config.createIndex) // TODO do not alway re-create the index
    private val indexSearcher: IndexSearcher = new LuceneSearcher(indexCommitter.asInstanceOf[LuceneCommitter].getIndexWriter)

    private var indexChanged = false
    private val cache: mutable.Queue[IndexDoc] = new mutable.Queue
    private val updateWebsiteQueue: mutable.Queue[(String,String)] = new mutable.Queue
    private val updateImageQueue: mutable.Queue[(String,String)] = new mutable.Queue
    private val updateLinkQueue: mutable.Queue[(String,String)] = new mutable.Queue

    private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.index.dispatcher")

    private var supervisor: ActorRef = _

    // kickoff the committing play
    context.system.scheduler.schedule(config.commitInterval, config.commitInterval, self, CommitIndex)

    override def postRestart(cause: Throwable): Unit = {
        log.info("{} has been restarted or resumed", self.path.name)
        cause match {
            case e: SearchException =>
                log.error("Error trying to search the index; reason: {}", e.getMessage)
            case e: Exception =>
                log.error("Unhandled Exception : {}", e.getMessage, e)
                sender ! SearchResults("UNKNOWN", ResultWrapper.empty()) // TODO besser eine neue antwortmessage a la ErrorIndexResult und entsprechend den fehler in der UI anzeigen zu können
                //currQuery = ""
        }
        super.postRestart(cause)
    }

    override def postStop: Unit = {
        Option(indexCommitter).foreach(_.destroy())
        Option(indexSearcher).foreach(_.destroy())

        log.info("shutting down")
    }

    override def receive: Receive = {

        case ActorRefSupervisor(ref) =>
            log.debug("Received ActorRefSupervisor(_)")
            supervisor = ref

        case CommitIndex =>
            commitIndexIfChanged()

        case AddDocIndexEvent(doc) =>
            log.debug("Received IndexStoreAddDoc({})", doc.getExo)
            cache.enqueue(doc)

        case UpdateDocWebsiteDataIndexEvent(exo, html) =>
            log.debug("Received IndexStoreUpdateDocWebsiteData({},_)", exo)
            updateWebsiteQueue.enqueue((exo,html))

        // TODO this fix is not done in the Directory and only correct data gets send to the index anyway...
        case UpdateDocImageIndexEvent(exo, image) =>
            log.debug("Received IndexStoreUpdateDocImage({},{})", exo, image)
            updateImageQueue.enqueue((exo, image))

        case UpdateDocLinkIndexEvent(exo, link) =>
            log.debug("Received IndexStoreUpdateDocLink({},'{}')", exo, link)
            updateLinkQueue.enqueue((exo, link))

        case SearchIndex(query, page, size) =>
            log.debug("Received SearchIndex('{}',{},{}) message", query, page, size)

            var currQuery = query // make a copy in case of an exception
            val origSender = sender()

            Future {
                var results: ResultWrapper = null
                blocking {
                    results = indexSearcher.search(currQuery, page, size)
                }

                if (results.getTotalHits > 0){
                    origSender ! SearchResults(currQuery,results)
                } else {
                    log.warning("No Podcast matching query: '{}' found in the index", query)
                    //sender ! NoIndexResultsFound(query)
                    origSender ! SearchResults(currQuery,ResultWrapper.empty())
                }

                currQuery = "" // wipe the copy
            }

    }

    private def commitIndexIfChanged(): Unit = {
        var committed = false
        //if (indexChanged) {
        if (cache.nonEmpty) {
            log.debug("Committing Index due to pending changes")

            for (doc <- cache) {
                indexCommitter.add(doc)
            }
            indexCommitter.commit()
            //indexChanged = false
            cache.clear()

            committed = true
            log.debug("Finished Index due to pending changes")
        }

        if (updateWebsiteQueue.nonEmpty) {
            log.debug("Processing pending entries in website queue")
            indexSearcher.refresh()
            processWebsiteQueue(updateWebsiteQueue)
            indexCommitter.commit()
            committed = true
            log.debug("Finished pending entries in website queue")
        }

        if (updateImageQueue.nonEmpty) {
            log.debug("Processing pending entries in image queue")
            indexSearcher.refresh()
            processImageQueue(updateImageQueue)
            indexCommitter.commit()
            committed = true
            log.debug("Finished pending entries in image queue")
        }

        if (updateLinkQueue.nonEmpty) {
            log.debug("Processing pending entries in link queue")
            indexSearcher.refresh()
            processLinkQueue(updateLinkQueue)
            indexCommitter.commit()
            committed = true
            log.debug("Finished pending entries in link queue")
        }

        if (committed) {
            indexSearcher.refresh()
        }
    }

    private def processWebsiteQueue(queue: mutable.Queue[(String,String)]): Unit = {
        if (queue.nonEmpty) {
            val (exo,html) = queue.dequeue()
            val entry = indexSearcher.findByExo(exo).asScala.map(_.asInstanceOf[ImmutableIndexDoc])
            entry match {
                case Some(doc) => indexCommitter.update(doc.withWebsiteData(html))
                case None      => log.error("Could not retrieve from index for update website (EXO) : {}", exo)
            }

            processWebsiteQueue(queue)
        }
    }

    private def processImageQueue(queue: mutable.Queue[(String,String)]): Unit = {
        if (queue.nonEmpty) {
            val (exo,image) = queue.dequeue()
            val entry = indexSearcher.findByExo(exo).asScala.map(_.asInstanceOf[ImmutableIndexDoc])
            entry match {
                case Some(doc) => indexCommitter.update(doc.withImage(image))
                case None      => log.error("Could not retrieve from index for update image (EXO) : {}", exo)
            }

            processImageQueue(queue)
        }
    }

    private def processLinkQueue(queue: mutable.Queue[(String,String)]): Unit = {
        if (queue.nonEmpty) {
            val (exo,link) = queue.dequeue()
            val entry = indexSearcher.findByExo(exo).asScala.map(_.asInstanceOf[ImmutableIndexDoc])
            entry match {
                case Some(doc) => indexCommitter.update(doc.withLink(link))
                case None      => log.error("Could not retrieve from index for update link (EXO) : {}", exo)
            }

            processLinkQueue(queue)
        }
    }

}
