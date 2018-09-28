package io.disposia.engine.parser

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Send}
import com.typesafe.config.ConfigFactory
import io.disposia.engine.EngineProtocol._
import io.disposia.engine.catalog.CatalogBroker
import io.disposia.engine.catalog.CatalogStore._
import io.disposia.engine.crawler.Crawler.{DownloadWithHeadCheck, WebsiteFetchJob}
import io.disposia.engine.domain.{Episode, FeedStatus}
import io.disposia.engine.exception.FeedParsingException
import io.disposia.engine.index.IndexStore.{AddDocIndexEvent, IndexEvent, UpdateDocWebsiteDataIndexEvent}
import io.disposia.engine.mapper.{EpisodeMapper, IndexMapper, PodcastMapper}
import io.disposia.engine.parse.api.FyydDirectoryAPI
import io.disposia.engine.parse.rss.RomeFeedParser
import io.disposia.engine.parser.Parser.{ParseFyydEpisodes, ParseNewPodcastData, ParseUpdateEpisodeData, ParseWebsiteData}
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

import scala.collection.JavaConverters._

object ParserWorker {
  def name(workerIndex: Int): String = "worker-" + workerIndex
  def props(config: ParserConfig): Props =
    Props(new ParserWorker(config)).withDispatcher("echo.parser.dispatcher")
}

class ParserWorker (config: ParserConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

  private val podcastMapper = PodcastMapper.INSTANCE
  private val episodeMapper = EpisodeMapper.INSTANCE
  private val indexMapper = IndexMapper.INSTANCE

  private var catalog: ActorRef = _
  private var index: ActorRef = _
  private var crawler: ActorRef = _
  private var supervisor: ActorRef = _

  private val fyydAPI: FyydDirectoryAPI = new FyydDirectoryAPI()

  private var currFeedUrl = ""
  private var currPodcastExo = ""

  override def postRestart(cause: Throwable): Unit = {
    log.warning("{} has been restarted or resumed", self.path.name)
    cause match {
      case e: FeedParsingException =>
        log.error("FeedParsingException occured while processing feed : {}", currFeedUrl)
        //directoryStore ! FeedStatusUpdate(currPodcastExo, currFeedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)
        val catalogEvent = FeedStatusUpdate(currPodcastExo, currFeedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)
        //emitCatalogEvent(catalogEvent)
        catalog ! catalogEvent
        currPodcastExo = ""
        currFeedUrl = ""
      case e: java.lang.StackOverflowError =>
        log.error("StackOverflowError parsing : {} ; reason: {}", currFeedUrl, e.getMessage, e)
      case e: Exception =>
        log.error("Unhandled Exception : {}", e.getMessage, e)
    }
    super.postRestart(cause)
  }

  override def postStop: Unit = {
    log.info("shutting down")
  }

  override def receive: Receive = {

    case ActorRefCatalogStoreActor(ref) =>
      log.debug("Received ActorRefCatalogStoreActor(_)")
      catalog = ref

    case ActorRefIndexStoreActor(ref) =>
      log.debug("Received ActorRefIndexStoreActor(_)")
      index = ref

    case ActorRefCrawlerActor(ref) =>
      log.debug("Received ActorRefCrawlerActor(_)")
      crawler = ref

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      supervisor ! ReportWorkerStartupComplete

    case ParseNewPodcastData(feedUrl, podcastExo, feedData) => onParseNewPodcastData(feedUrl, podcastExo, feedData)

    case ParseUpdateEpisodeData(feedUrl, podcastExo, episodeFeedData) => onParseUpdateEpisodeData(feedUrl, podcastExo, episodeFeedData)

    case ParseWebsiteData(exo, html) => onParseWebsiteData(exo, html)

    case ParseFyydEpisodes(podcastExo, json) => onParseFyydEpisodes(podcastExo, json)

  }

  private def onParseNewPodcastData(feedUrl: String, podcastExo: String, feedData: String): Unit = {
    log.debug("Received ParseNewPodcastData for feed: " + feedUrl)

    currFeedUrl = feedUrl
    currPodcastExo = podcastExo

    parse(podcastExo, feedUrl, feedData, isNewPodcast = true)

    currFeedUrl = ""
    currPodcastExo = ""
  }

  private def onParseUpdateEpisodeData(feedUrl: String, podcastExo: String, episodeFeedData: String): Unit = {
    log.debug("Received ParseEpisodeData({},{},_)", feedUrl, podcastExo)

    currFeedUrl = feedUrl
    currPodcastExo = podcastExo

    parse(podcastExo, feedUrl, episodeFeedData, isNewPodcast = false)

    currFeedUrl = ""
    currPodcastExo = ""
  }

  private def onParseWebsiteData(exo: String, html: String): Unit = {
    log.debug("Received ParseWebsiteData({},_)", exo)

    val readableText = Jsoup.parse(html).text()

    val indexEvent = UpdateDocWebsiteDataIndexEvent(exo, readableText)
    //mediator ! Publish(indexEventStream, indexEvent)
    index ! indexEvent
  }

  private def onParseFyydEpisodes(podcastExo: String, json: String): Unit = {
    log.debug("Received ParseFyydEpisodes({},_)", podcastExo)

    val episodes: List[Episode] = fyydAPI.getEpisodes(json).asScala.toList
    log.info("Loaded {} episodes from fyyd for podcast : {}", episodes.size, podcastExo)
    for(episode <- episodes){
      registerEpisode(podcastExo, episode)
    }
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

  private def parse(podcastExo: String, feedUrl: String, feedData: String, isNewPodcast: Boolean): Unit = {

    val parser = RomeFeedParser.of(feedData)
    Option(parser.getPodcast) match {
      case Some(podcast) =>
        val p = podcastMapper.toModifiable(podcast)
        // TODO try-catch for Feedparseerror here, send update
        // directoryStore ! FeedStatusUpdate(feedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)

        p.setExo(podcastExo)

        Option(p.getTitle).foreach(t => p.setTitle(t.trim))
        Option(p.getDescription).foreach(d => p.setDescription(Jsoup.clean(d, Whitelist.basic())))

        if (isNewPodcast) {

          /* TODO
          // experimental: this works but has terrible performance and assumes we have a GUI app
          Option(p.getItunesImage).foreach(img => {
              p.setItunesImage(base64Image(img))
          })
          */

          val indexEvent = AddDocIndexEvent(indexMapper.toImmutable(p))
          //emitIndexEvent(indexEvent)
          index ! indexEvent

          // request that the podcasts website will get added to the index as well, if possible
          Option(p.getLink) match {
            case Some(link) =>
              crawler ! DownloadWithHeadCheck(p.getExo, link, WebsiteFetchJob())
            case None => log.debug("No link set for podcast {} --> no website data will be added to the index", p.getExo)
          }
        }

        // we always update a podcasts metadata, this likely may have changed (new descriptions, etc)
        val catalogEvent = UpdatePodcast(podcastExo, feedUrl, p.toImmutable)
        //emitCatalogEvent(catalogEvent)
        catalog ! catalogEvent

        // check for "new" episodes: because this is a new Podcast, all episodes will be new and registered
        Option(parser.getEpisodes) match {
          case Some(es) =>
            for(e <- es.asScala){
              registerEpisode(podcastExo, e)
            }
          case None => log.warning("Parsing generated a NULL-List[Episode] for feed: {}", feedUrl)
        }
      case None => log.warning("Parsing generated a NULL-PodcastDocument for feed: {}", feedUrl)
    }
  }

  private def registerEpisode(podcastExo: String, episode: Episode): Unit = {

    val e = episodeMapper.toModifiable(episode)

    // cleanup some potentially markuped texts
    Option(e.getTitle).foreach(t => e.setTitle(t.trim))
    Option(e.getDescription).foreach(d => e.setDescription(Jsoup.clean(d, Whitelist.basic())))
    Option(e.getContentEncoded).foreach(c => e.setContentEncoded(Jsoup.clean(c, Whitelist.basic())))

    /* TODO
    // experimental: this works but has terrible performance and assumes we have a GUI app
    Option(e.getItunesImage).foreach(img => {
        e.setItunesImage(base64Image(img))
    })
    */

    val catalogCommand = RegisterEpisodeIfNew(podcastExo, e.toImmutable)
    //sendCatalogCommand(catalogCommand)
    catalog ! catalogCommand
  }

  /* TODO this code works but produces bad output and is super slow!
  private def base64Image(imageUrl: String): String = {
      try {
          val sourceImage: BufferedImage = ImageIO.read(new URL(imageUrl))
          if(sourceImage == null) return null
          val resampleOp: ResampleOp = new ResampleOp(400,400)
          val scaledImage = resampleOp.filter(sourceImage, null)
          val outputStream: ByteArrayOutputStream = new ByteArrayOutputStream()
          ImageIO.write(scaledImage, "jpg", outputStream)
          val base64 = Base64.getEncoder.encodeToString(outputStream.toByteArray)
          "data:image/png;base64," + base64
      } catch {
          case e: IOException =>
              null
      }
  }
  */

}
