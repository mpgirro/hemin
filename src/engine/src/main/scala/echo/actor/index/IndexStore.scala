package echo.actor.index

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import com.typesafe.config.ConfigFactory
import echo.actor.index.IndexProtocol._
import echo.core.domain.dto.ImmutableIndexDocDTO
import echo.core.exception.SearchException
import echo.core.index.{IndexCommitter, IndexSearcher, LuceneCommitter, LuceneSearcher}

import scala.collection.mutable
import scala.compat.java8.OptionConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */
object IndexStore {
    def name(storeIndex: Int): String = "store-" + storeIndex
    def props(indexPath: String, createIndex: Boolean): Props = {
        Props(new IndexStore(indexPath, createIndex)).withDispatcher("echo.index.dispatcher")
    }
}

class IndexStore (indexPath: String,
                  createIndex: Boolean) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val COMMIT_INTERVAL: FiniteDuration = Option(CONFIG.getInt("echo.index.commit-interval")).getOrElse(3).seconds
    /*
    private val INDEX_PATH: String = Option(CONFIG.getString("echo.index.lucene-path")).getOrElse("index")
    */

    private val mediator = DistributedPubSub(context.system).mediator

    private val indexCommitter: IndexCommitter = new LuceneCommitter(indexPath, createIndex) // TODO do not alway re-create the index
    private val indexSearcher: IndexSearcher = new LuceneSearcher(indexCommitter.asInstanceOf[LuceneCommitter].getIndexWriter)

    private var indexChanged = false
    private val updateWebsiteQueue: mutable.Queue[(String,String)] = new mutable.Queue
    private val updateImageQueue: mutable.Queue[(String,String)] = new mutable.Queue
    private val updateLinkQueue: mutable.Queue[(String,String)] = new mutable.Queue

    private var currQuery: String = ""

    private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.index.dispatcher")

    // kickoff the committing play
    context.system.scheduler.scheduleOnce(COMMIT_INTERVAL, self, CommitIndex)

    override def postRestart(cause: Throwable): Unit = {
        log.info("{} has been restarted or resumed", self.path.name)
        cause match {
            case e: SearchException =>
                log.error("Error trying to search the index; reason: {}", e.getMessage)
            case e: Exception =>
                log.error("Unhandled Exception : {}", e.getMessage, e)
                sender ! NoIndexResultsFound(currQuery) // TODO besser eine neue antwortmessage a la ErrorIndexResult und entsprechend den fehler in der UI anzeigen zu können
                currQuery = ""
        }
        super.postRestart(cause)
    }

    override def postStop: Unit = {
        Option(indexCommitter).foreach(_.destroy())
        Option(indexSearcher).foreach(_.destroy())

        log.info("shutting down")
    }

    override def receive: Receive = {

        case CommitIndex =>
            commitIndexIfChanged()
            context.system.scheduler.scheduleOnce(COMMIT_INTERVAL, self, CommitIndex)

        case AddDocIndexEvent(doc) =>
            log.debug("Received IndexStoreAddDoc({})", doc.getExo)

            indexCommitter.add(doc)
            indexChanged = true

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
            currQuery = query // make a copy in case of an exception

            indexSearcher.refresh()
            val results = indexSearcher.search(query, page, size)
            if(results.getTotalHits > 0){
                sender ! IndexResultsFound(query,results)
            } else {
                log.warning("No Podcast matching query: '{}' found in the index", query)
                sender ! NoIndexResultsFound(query)
            }

            currQuery = "" // wipe the copy

    }

    private def commitIndexIfChanged(): Unit = {
        if (indexChanged) {
            log.debug("Committing Index due to pending changes")
            indexCommitter.commit()
            indexChanged = false
            log.debug("Finished Index due to pending changes")
        }

        if (updateWebsiteQueue.nonEmpty) {
            log.debug("Processing pending entries in website queue")
            indexSearcher.refresh()
            processWebsiteQueue(updateWebsiteQueue)
            indexCommitter.commit()
            log.debug("Finished pending entries in website queue")
        }

        if (updateImageQueue.nonEmpty) {
            log.debug("Processing pending entries in image queue")
            indexSearcher.refresh()
            processImageQueue(updateImageQueue)
            indexCommitter.commit()
            log.debug("Finished pending entries in image queue")
        }

        if (updateLinkQueue.nonEmpty) {
            log.debug("Processing pending entries in link queue")
            indexSearcher.refresh()
            processLinkQueue(updateLinkQueue)
            indexCommitter.commit()
            log.debug("Finished pending entries in link queue")
        }
    }

    private def processWebsiteQueue(queue: mutable.Queue[(String,String)]): Unit = {
        if (queue.nonEmpty) {
            val (exo,html) = queue.dequeue()
            val entry = indexSearcher.findByExo(exo).asScala.map(_.asInstanceOf[ImmutableIndexDocDTO])
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
            val entry = indexSearcher.findByExo(exo).asScala.map(_.asInstanceOf[ImmutableIndexDocDTO])
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
            val entry = indexSearcher.findByExo(exo).asScala.map(_.asInstanceOf[ImmutableIndexDocDTO])
            entry match {
                case Some(doc) => indexCommitter.update(doc.withLink(link))
                case None      => log.error("Could not retrieve from index for update link (EXO) : {}", exo)
            }

            processLinkQueue(queue)
        }
    }

}
