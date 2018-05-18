package echo.actor.crawler

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Send}
import akka.stream._
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.actor.directory.DirectoryProtocol._
import echo.actor.index.IndexProtocol.{IndexEvent, UpdateDocLinkIndexEvent}
import echo.core.domain.feed.FeedStatus
import echo.core.exception.EchoException
import echo.core.http.HttpClient
import echo.core.parse.api.FyydAPI

import scala.compat.java8.OptionConverters._
import scala.concurrent.blocking
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */

object CrawlerWorker {
    def name(workerIndex: Int): String = "worker-" + workerIndex
    def props(): Props = Props(new CrawlerWorker()).withDispatcher("echo.crawler.dispatcher")
}

class CrawlerWorker extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private val WEBSITE_JOBS: Boolean = Option(CONFIG.getBoolean("echo.crawler.website-jobs")).getOrElse(false)

    // TODO define CONN_TIMEOUT
    // TODO define READ_TIMEOUT

    private val DOWNLOAD_TIMEOUT = 10 // TODO read from config
    private val DOWNLOAD_MAXBYTES = 5242880 // = 5  * 1024 * 1024 // TODO load from config file

    // important, or we will experience starvation on processing many feeds at once
    private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.crawler.dispatcher")

    private implicit val actorSystem: ActorSystem = context.system
    private implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem))

    private val directoryEventStream = CONFIG.getString("echo.directory.event-stream")
    private val indexEventStream = CONFIG.getString("echo.index.event-stream")
    private val mediator = DistributedPubSub(context.system).mediator

    private var parser: ActorRef = _

    private val fyydAPI: FyydAPI = new FyydAPI()

    private var httpClient: HttpClient = new HttpClient(DOWNLOAD_TIMEOUT, DOWNLOAD_MAXBYTES)

    private var currUrl: String = _
    private var currJob: FetchJob = _

    override def postStop: Unit = {
        httpClient.close()
    }

    override def postRestart(cause: Throwable): Unit = {
        log.info("{} has been restarted or resumed", self.path.name)
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

        case ActorRefParserActor(ref) =>
            log.debug("Received ActorRefIndexerActor(_)")
            parser = ref

        case DownloadWithHeadCheck(exo, url, job) =>

            this.currUrl = url
            this.currJob = job

            job match {
                case WebsiteFetchJob() =>
                    if (WEBSITE_JOBS) {
                        log.info("Received DownloadWithHeadCheck({}, '{}', {})", exo, url, job.getClass.getSimpleName)
                        headCheck(exo, url, job)
                    }
                case _ =>
                    log.info("Received DownloadWithHeadCheck({}, '{}', {})", exo, url, job.getClass.getSimpleName)
                    headCheck(exo, url, job)
            }


        case DownloadContent(exo, url, job, encoding) =>
            log.debug("Received Download({},'{}',{},{})", exo, url, job.getClass.getSimpleName, encoding)

            this.currUrl = url
            this.currJob = job

            fetchContent(exo, url, job, encoding) // TODO send encoding via message

        case CrawlFyyd(count) => onCrawlFyyd(count)

        case LoadFyydEpisodes(podcastId, fyydId) => onLoadFyydEpisodes(podcastId, fyydId)

    }

    private def sendDirectoryCommand(command: DirectoryCommand): Unit = {
        mediator ! Send("/user/node/directory", command, localAffinity = true)
    }

    private def emitDirectoryEvent(event: DirectoryEvent): Unit = {
        mediator ! Publish(directoryEventStream, event)
    }

    private def onCrawlFyyd(count: Int) = {
        log.debug("Received CrawlFyyd({})", count)

        val feeds = fyydAPI.getFeedUrls(count)

        log.debug("Received {} feeds from {}", feeds.size, fyydAPI.getURL)
        log.debug("Proposing these feeds to the internal directory now")

        val it = feeds.iterator()
        while (it.hasNext) {
            val directoryCommand = ProposeNewFeed(it.next())
            sendDirectoryCommand(directoryCommand)
        }
    }

    private def onLoadFyydEpisodes(podcastId: String, fyydId: Long) = {
        log.debug("Received LoadFyydEpisodes({},'{}')", podcastId, fyydId)

        val json = fyydAPI.getEpisodesByPodcastIdJSON(fyydId)
        parser ! ParseFyydEpisodes(podcastId, json)
    }

    private def emitIndexEvent(event: IndexEvent): Unit = {
        mediator ! Publish(indexEventStream, event)
    }

    private def sendErrorNotificationIfFeasable(exo: String, url: String, job: FetchJob): Unit = {
        job match {
            case WebsiteFetchJob() => // do nothing...
            case _ =>
                val directoryEvent = FeedStatusUpdate(exo, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_ERROR)
                emitDirectoryEvent(directoryEvent)
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
                                val directoryEvent = UpdateLinkByExo(exo, href)
                                emitDirectoryEvent(directoryEvent)

                                val indexEvent = UpdateDocLinkIndexEvent(exo, href)
                                emitIndexEvent(indexEvent)
                            }

                            // we always download websites, because we only do it once anyway
                            self ! DownloadContent(exo, href, job, encoding) // TODO
                            //fetchContent(exo, href, job, encoding)

                        case _ =>
                            // if the feed moved to a new URL, we will inform the directory, so
                            // it will use the new location starting with the next update cycle
                            if (!url.equals(href)) {
                                //directoryStore ! UpdateFeedUrl(url, href)
                                val directoryEvent = UpdateFeedUrl(url, href)
                                emitDirectoryEvent(directoryEvent)
                            }

                            /*
                             * TODO
                             * here I have to do some voodoo with etag/lastMod to
                             * determine weither the feed changed and I really need to redownload
                             */
                            self ! DownloadContent(exo, href, job, encoding) // TODO
                            //fetchContent(exo, href, job, encoding)
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
                    val directoryEvent = FeedStatusUpdate(exo, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
                    emitDirectoryEvent(directoryEvent)

                case UpdateEpisodesFetchJob(etag, lastMod) =>
                    parser ! ParseUpdateEpisodeData(url, exo, data)
                    val directoryEvent = FeedStatusUpdate(exo, url, LocalDateTime.now(), FeedStatus.DOWNLOAD_SUCCESS)
                    emitDirectoryEvent(directoryEvent)

                case WebsiteFetchJob() =>
                    parser ! ParseWebsiteData(exo, data)
            }
        }
    }

}
