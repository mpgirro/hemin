package io.hemin.engine.crawler

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.stream._
import io.hemin.engine.catalog.CatalogStore._
import io.hemin.engine.crawler.Crawler._
import io.hemin.engine.crawler.api._
import io.hemin.engine.crawler.http.HttpClient
import io.hemin.engine.index.IndexStore.UpdateDocLinkIndexEvent
import io.hemin.engine.model.FeedStatus
import io.hemin.engine.node.Node._
import io.hemin.engine.parser.Parser._
import io.hemin.engine.util.TimeUtil

import scala.concurrent.{ExecutionContext, blocking}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object CrawlerWorker {
  def name(workerIndex: Int): String = "worker-" + workerIndex
  def props(config: CrawlerConfig): Props =
    Props(new CrawlerWorker(config))
      .withDispatcher(config.dispatcher)
      .withMailbox(config.mailbox)
}

class CrawlerWorker (config: CrawlerConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher : {}", self.path.name, context.system.dispatchers.lookup(context.props.dispatcher))
  log.debug("{} running with mailbox : {}", self.path.name, context.system.mailboxes.lookup(context.props.mailbox))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private implicit val actorSystem: ActorSystem = context.system
  private implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem))

  private var catalog: ActorRef = _
  private var index: ActorRef = _
  private var parser: ActorRef = _
  private var supervisor: ActorRef = _

  private val httpClient = new HttpClient(config.downloadTimeout, config.downloadMaxBytes)

  // TODO implement the API's and allow them to be trigger by the CLI with respective messages
  private val fyyd = new FyydAPI()
  private val gpodder = new GpodderAPI()
  private val panoptikum = new PanoptikumAPI()
  private val podbay = new PodbayFmAPI()
  private val podcastpedia = new PodcastpediaAPI()

  override def postStop: Unit = {
    log.debug("shutting down")

    httpClient.close()
  }

  override def postRestart(cause: Throwable): Unit = {
    log.warning("{} has been restarted or resumed", self.path.name)
    cause match {
      case ex: Exception =>
        log.error("Unhandled Exception : {}", ex.getMessage)
        ex.printStackTrace()
    }
    super.postRestart(cause)
  }

  override def receive: Receive = {

    case ActorRefCatalogStoreActor(ref) =>
      log.debug("Received ActorRefCatalogStoreActor(_)")
      catalog = ref

    case ActorRefParserActor(ref) =>
      log.debug("Received ActorRefParserActor(_)")
      parser = ref

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      supervisor ! ReportWorkerStartupComplete

    case DownloadWithHeadCheck(id, url, job) =>
      (job, config.fetchWebsites) match {
        case (WebsiteFetchJob(), false) => // do nothing
        case (_, _) =>
          log.info("Received DownloadWithHeadCheck({}, '{}', {})", id, url, job.getClass.getSimpleName)
          headCheck(id, url, job)
      }

    case DownloadContent(id, url, job, encoding) =>
      log.debug("Received Download({},'{}',{},{},_)", id, url, job.getClass.getSimpleName, encoding)
      fetchContent(id, url, job, encoding) // TODO send encoding via message

    case CrawlFyyd(count) =>
      //onCrawlFyyd(count)

    case LoadFyydEpisodes(podcastId, fyydId) =>
      //onLoadFyydEpisodes(podcastId, fyydId)

  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
  }

  /*
  private def sendCatalogCommand(command: CatalogCommand): Unit = {
      mediator ! Send("/user/node/"+CatalogBroker.name, command, localAffinity = true)
  }

  private def emitCatalogEvent(event: CatalogEvent): Unit = {
      mediator ! Publish(catalogEventStream, event)
  }

  private def emitIndexEvent(event: IndexEvent): Unit = {
      mediator ! Publish(indexEventStream, event)
  }
  */

  /*
  private def onCrawlFyyd(count: Int) = {
    log.debug("Received CrawlFyyd({})", count)

    val feeds = fyydAPI.getFeedUrls(count)

    log.debug("Received {} feeds from {}", feeds.size, fyydAPI.getURL)
    log.debug("Proposing these feeds to the internal catalog now")

    val it = feeds.iterator()
    while (it.hasNext) {
      val catalogCommand = ProposeNewFeed(it.next())
      //sendCatalogCommand(catalogCommand)
      catalog ! catalogCommand
    }
  }

  private def onLoadFyydEpisodes(podcastId: String, fyydId: Long) = {
    log.debug("Received LoadFyydEpisodes({},'{}')", podcastId, fyydId)

    val json = fyydAPI.getEpisodesByPodcastIdJSON(fyydId)
    parser ! ParseFyydEpisodes(podcastId, json)
  }
  */

  private def sendErrorNotificationIfFeasable(id: String, url: String, job: FetchJob): Unit = {
    job match {
      case WebsiteFetchJob() => // do nothing...
      case _ =>
        val catalogEvent = FeedStatusUpdate(id, url, TimeUtil.now, FeedStatus.DownloadError)
        //emitCatalogEvent(catalogEvent)
        catalog ! catalogEvent
    }
  }

  private def headCheck(id: String, url: String, job: FetchJob): Unit = {
    blocking {
      httpClient.headCheck(url, job.mimeCheck) match {
        case Success(headResult) =>
          val encoding = headResult.contentEncoding

          val location = headResult.location

          // TODO check if eTag differs from last known value

          // TODO check if lastMod differs from last known value

          location match {
            case Some(href) =>
              log.debug("Sending message to download content : {}", href)
              job match {
                case WebsiteFetchJob() =>
                  // if the link in the feed is redirected (which is often the case due
                  // to some feed analytic tools, we set our records to the new location
                  if (!url.equals(href)) {
                    //directoryStore ! UpdateLinkByExo(exo, href)
                    val catalogEvent = UpdateLinkById(id, href)
                    //emitCatalogEvent(catalogEvent)
                    catalog ! catalogEvent

                    val indexEvent = UpdateDocLinkIndexEvent(id, href)
                    //emitIndexEvent(indexEvent)
                    index ! indexEvent
                  }

                  // we always download websites, because we only do it once anyway
                  self ! DownloadContent(id, href, job, encoding) // TODO
                //fetchContent(exo, href, job, encoding)

                case _ =>
                  // if the feed moved to a new URL, we will inform the directory, so
                  // it will use the new location starting with the next update cycle
                  if (!url.equals(href)) {
                    val catalogEvent = UpdateFeedUrl(url, href)
                    //emitCatalogEvent(catalogEvent)
                    catalog ! catalogEvent
                  }

                  /*
                   * TODO
                   * here I have to do some voodoo with etag/lastMod to
                   * determine weither the feed changed and I really need to redownload
                   */
                  self ! DownloadContent(id, href, job, encoding) // TODO
              }
            case None =>
              log.error("We did not get any location-url after evaluating response --> cannot proceed download without one")
              sendErrorNotificationIfFeasable(id, url, job)
          }
        case Failure(ex) => log.error("Error on HEAD check on URL '{}' ; reason : {}", url, ex.getMessage)
      }
    }
  }

  /**
    *
    * Docs for STTP: http://sttp.readthedocs.io/en/latest/
    *
    * @param id
    * @param url
    * @param job
    */
  private def fetchContent(id: String, url: String, job: FetchJob, encoding: Option[String]): Unit = {
    blocking {
      httpClient.fetchContent(url, encoding, job.mimeCheck) match {
        case Success((data, mime, enc)) =>
          job match {
            case NewPodcastFetchJob() =>
              parser ! ParseNewPodcastData(url, id, asString(data, enc))
              val catalogEvent = FeedStatusUpdate(id, url, TimeUtil.now, FeedStatus.DownloadSuccess)
              //emitCatalogEvent(catalogEvent)
              catalog ! catalogEvent

            case UpdateEpisodesFetchJob(etag, lastMod) =>
              parser ! ParseUpdateEpisodeData(url, id, asString(data, enc))
              val catalogEvent = FeedStatusUpdate(id, url, TimeUtil.now, FeedStatus.DownloadSuccess)
              //emitCatalogEvent(catalogEvent)
              catalog ! catalogEvent

            case WebsiteFetchJob() => parser ! ParseWebsiteData(id, asString(data, enc))

            case ImageFetchJob() => parser ! ParseImage(url, mime, enc, data)

          }
        case Failure(ex) =>
          log.error("Error fetching content from URL '{}' ; reason : {}", url, ex.getMessage)
          ex.printStackTrace()
          // TODO send a notification to catalogstore for a feed status update?
      }

    }
  }

  private def asString(data: Array[Byte], encoding: String): String = new String(data, encoding)

}
