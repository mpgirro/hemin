package io.disposia.engine.crawler

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Send}
import akka.stream._
import com.typesafe.config.ConfigFactory
import io.disposia.engine.EngineProtocol._
import io.disposia.engine.catalog.CatalogBroker
import io.disposia.engine.catalog.CatalogStore._
import io.disposia.engine.crawler.Crawler._
import io.disposia.engine.domain.FeedStatus
import io.disposia.engine.exception.EchoException
import io.disposia.engine.index.IndexStore.{IndexEvent, UpdateDocLinkIndexEvent}
import io.disposia.engine.parse.api.FyydDirectoryAPI
import io.disposia.engine.parser.Parser.{ParseFyydEpisodes, ParseNewPodcastData, ParseUpdateEpisodeData, ParseWebsiteData}

import scala.compat.java8.OptionConverters._
import scala.concurrent.{ExecutionContext, blocking}
import scala.language.postfixOps

object CrawlerWorker {
  def name(workerIndex: Int): String = "worker-" + workerIndex
  def props(config: CrawlerConfig): Props = Props(new CrawlerWorker(config)).withDispatcher("echo.crawler.dispatcher")
}

class CrawlerWorker (config: CrawlerConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

  // important, or we will experience starvation on processing many feeds at once
  private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.crawler.dispatcher")

  private implicit val actorSystem: ActorSystem = context.system
  private implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem))

  private var catalog: ActorRef = _
  private var index: ActorRef = _
  private var parser: ActorRef = _
  private var supervisor: ActorRef = _

  private val fyydAPI: FyydDirectoryAPI = new FyydDirectoryAPI()
  private var httpClient: HttpClient = new HttpClient(config.downloadTimeout, config.downloadMaxBytes)

  private var currUrl: String = _
  private var currJob: FetchJob = _

  override def postStop: Unit = {
    httpClient.close()
  }

  override def postRestart(cause: Throwable): Unit = {
    log.warning("{} has been restarted or resumed", self.path.name)
    cause match {
      case e: EchoException =>
        log.error("HEAD response prevented fetching resource : {} [reason : {}]", currUrl, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
      case e: java.net.ConnectException =>
        log.error("java.net.ConnectException on : {} [msg : {}]", currUrl, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
      case e: java.net.SocketTimeoutException =>
        log.error("java.net.SocketTimeoutException on : {} [msg : {}]", currUrl, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
      case e: java.net.UnknownHostException =>
        log.error("java.net.UnknownHostException on : {} [msg : {}]", currUrl, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
      case e: javax.net.ssl.SSLHandshakeException =>
        log.error("javax.net.ssl.SSLHandshakeException on : {} [msg : {}]", currUrl, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
      case e: java.io.UnsupportedEncodingException =>
        log.error("java.io.UnsupportedEncodingException on : {} [msg : {}]", currUrl, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"))
      case e: Exception =>
        log.error("Unhandled Exception on {} : {}", currUrl, Option(e.getMessage).getOrElse("NO REASON GIVEN IN EXCEPTION"), e)
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

    case DownloadWithHeadCheck(exo, url, job) =>
      log.debug("Received Download({},'{}',{},_)", exo, url, job.getClass.getSimpleName)

      this.currUrl = url
      this.currJob = job

      job match {
        case WebsiteFetchJob() =>
          if (config.fetchWebsites) {
            log.info("Received DownloadWithHeadCheck({}, '{}', {})", exo, url, job.getClass.getSimpleName)
            headCheck(exo, url, job)
          }
        case _ =>
          log.info("Received DownloadWithHeadCheck({}, '{}', {})", exo, url, job.getClass.getSimpleName)
          headCheck(exo, url, job)
      }


    case DownloadContent(exo, url, job, encoding) =>
      log.debug("Received Download({},'{}',{},{},_)", exo, url, job.getClass.getSimpleName, encoding)

      this.currUrl = url
      this.currJob = job

      fetchContent(exo, url, job, encoding) // TODO send encoding via message

    case CrawlFyyd(count) =>
      onCrawlFyyd(count)

    case LoadFyydEpisodes(podcastId, fyydId) =>
      onLoadFyydEpisodes(podcastId, fyydId)

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

  private def sendErrorNotificationIfFeasable(exo: String, url: String, job: FetchJob): Unit = {
    job match {
      case WebsiteFetchJob() => // do nothing...
      case _ =>
        val catalogEvent = FeedStatusUpdate(exo, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
        //emitCatalogEvent(catalogEvent)
        catalog ! catalogEvent
    }
  }

  private def headCheck(exo: String, url: String, job: FetchJob): Unit = {
    blocking {
      val headResult = httpClient.headCheck(url)

      val encoding = headResult.getContentEncoding.asScala

      val location = headResult.getLocation.asScala

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
                val catalogEvent = UpdateLinkByExo(exo, href)
                //emitCatalogEvent(catalogEvent)
                catalog ! catalogEvent

                val indexEvent = UpdateDocLinkIndexEvent(exo, href)
                //emitIndexEvent(indexEvent)
                index ! indexEvent
              }

              // we always download websites, because we only do it once anyway
              self ! DownloadContent(exo, href, job, encoding) // TODO
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
              self ! DownloadContent(exo, href, job, encoding) // TODO
          }
        case None =>
          log.error("We did not get any location-url after evaluating response --> cannot proceed download without one")
          sendErrorNotificationIfFeasable(exo, url, job)
      }
    }
  }

  /**
    *
    * Docs for STTP: http://sttp.readthedocs.io/en/latest/
    *
    * @param exo
    * @param url
    * @param job
    */
  private def fetchContent(exo: String, url: String, job: FetchJob, encoding: Option[String]): Unit = {
    blocking {
      val data = httpClient.fetchContent(url, encoding.asJava)
      job match {
        case NewPodcastFetchJob() =>
          parser ! ParseNewPodcastData(url, exo, data)
          val catalogEvent = FeedStatusUpdate(exo, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
          //emitCatalogEvent(catalogEvent)
          catalog ! catalogEvent

        case UpdateEpisodesFetchJob(etag, lastMod) =>
          parser ! ParseUpdateEpisodeData(url, exo, data)
          val catalogEvent = FeedStatusUpdate(exo, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
          //emitCatalogEvent(catalogEvent)
          catalog ! catalogEvent

        case WebsiteFetchJob() =>
          parser ! ParseWebsiteData(exo, data)
      }
    }
  }

}
