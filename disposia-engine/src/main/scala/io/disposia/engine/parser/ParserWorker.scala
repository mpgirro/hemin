package io.disposia.engine.parser

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.LocalDateTime
import java.util.Formatter

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.disposia.engine.EngineProtocol._
import io.disposia.engine.catalog.CatalogStore._
import io.disposia.engine.crawler.Crawler.{DownloadWithHeadCheck, PodcastImageFetchJob, WebsiteFetchJob}
import io.disposia.engine.domain.{Episode, FeedStatus, Image}
import io.disposia.engine.exception.FeedParsingException
import io.disposia.engine.index.IndexStore.{AddDocIndexEvent, UpdateDocWebsiteDataIndexEvent}
import io.disposia.engine.parser.Parser._
import io.disposia.engine.parser.feed.RomeFeedParser
import io.disposia.engine.util.HashUtil
import io.disposia.engine.util.mapper.IndexMapper
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

object ParserWorker {
  def name(workerIndex: Int): String = "worker-" + workerIndex
  def props(config: ParserConfig): Props =
    Props(new ParserWorker(config)).withDispatcher("echo.parser.dispatcher")
}

class ParserWorker (config: ParserConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

  private var catalog: ActorRef = _
  private var index: ActorRef = _
  private var crawler: ActorRef = _
  private var supervisor: ActorRef = _

  //private val fyydAPI: FyydDirectoryAPI = new FyydDirectoryAPI()

  private var currFeedUrl = ""
  private var currPodcastId = ""

  override def postRestart(cause: Throwable): Unit = {
    log.warning("{} has been restarted or resumed", self.path.name)
    cause match {
      case e: FeedParsingException =>
        log.error("FeedParsingException occured while processing feed : {}", currFeedUrl)
        //directoryStore ! FeedStatusUpdate(currPodcastExo, currFeedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)
        val catalogEvent = FeedStatusUpdate(currPodcastId, currFeedUrl, LocalDateTime.now(), FeedStatus.PARSE_ERROR)
        //emitCatalogEvent(catalogEvent)
        catalog ! catalogEvent
        currPodcastId = ""
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

    case ParseNewPodcastData(feedUrl, podcastId, feedData) => onParseNewPodcastData(feedUrl, podcastId, feedData)

    case ParseUpdateEpisodeData(feedUrl, podcastId, episodeFeedData) => onParseUpdateEpisodeData(feedUrl, podcastId, episodeFeedData)

    case ParseWebsiteData(id, html) => onParseWebsiteData(id, html)

    case ParseFyydEpisodes(podcastId, json) => onParseFyydEpisodes(podcastId, json)

    case ParsePodcastImage(podcastId, imageData) => onParsePodcastImage(podcastId, imageData)

    case ParseEpisodeImage(episodeId, imageData) => onParseEpisodeImage(episodeId, imageData)

    case unhandled => log.warning("Received unhandled message of type : {}", unhandled.getClass)

  }

  private def onParseNewPodcastData(feedUrl: String, podcastId: String, feedData: String): Unit = {
    log.debug("Received ParseNewPodcastData for feed: " + feedUrl)

    currFeedUrl = feedUrl
    currPodcastId = podcastId

    parse(podcastId, feedUrl, feedData, isNewPodcast = true)

    currFeedUrl = ""
    currPodcastId = ""
  }

  private def onParseUpdateEpisodeData(feedUrl: String, podcastId: String, episodeFeedData: String): Unit = {
    log.debug("Received ParseEpisodeData({},{},_)", feedUrl, podcastId)

    currFeedUrl = feedUrl
    currPodcastId = podcastId

    parse(podcastId, feedUrl, episodeFeedData, isNewPodcast = false)

    currFeedUrl = ""
    currPodcastId = ""
  }

  private def onParseWebsiteData(id: String, html: String): Unit = {
    log.debug("Received ParseWebsiteData({},_)", id)

    val readableText = Jsoup.parse(html).text()

    val indexEvent = UpdateDocWebsiteDataIndexEvent(id, readableText)
    //mediator ! Publish(indexEventStream, indexEvent)
    index ! indexEvent
  }

  private def onParseFyydEpisodes(podcastId: String, json: String): Unit = {
    log.debug("Received ParseFyydEpisodes({},_)", podcastId)

    /*
    val episodes: List[OldEpisode] = fyydAPI.getEpisodes(json).asScala.toList
    log.info("Loaded {} episodes from fyyd for podcast : {}", episodes.size, podcastId)
    for(episode <- episodes){
      registerEpisode(podcastId, episode)
    }
    */

    throw new UnsupportedOperationException("currently not implemented")
  }

  private def onParsePodcastImage(podcastId: String, imageData: String): Unit = {
    log.debug("Received ParsePodcastImage({},_)", podcastId)

    val image = imageFromData(podcastId, imageData)

    // TODO send message to Catalog
  }

  private def onParseEpisodeImage(episodeId: String, imageData: String): Unit = {
    log.debug("Received ParseEpisodeImage({},_)", episodeId)

    val image = imageFromData(episodeId, imageData)

    // TODO send message to Catalog
  }

  private def imageFromData(associateId: String, imageData: String): Image = {
    val image = com.sksamuel.scrimage.Image.fromStream(inputStreamFromString(imageData))
    val data = transform(image)

    // TODO set more fields of following instance!
    Image(
      associateId = Some(associateId),
      data        = Some(data),
      hash        = Some(HashUtil.sha1(data)),
    )
  }

  private def inputStreamFromString(data: String): InputStream =
    new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8.name))

  private def transform(image: com.sksamuel.scrimage.Image): Array[Byte] = image
    .cover(500, 500)
    .bound(500, 500)
    .bytes

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

  private def parse(podcastId: String, feedUrl: String, feedData: String, isNewPodcast: Boolean): Unit = {

    val parser = new RomeFeedParser(feedData)
    val p = parser.podcast.copy(
      id          = Some(podcastId),
      title       = parser.podcast.title.map(_.trim),
      description = parser.podcast.description.map(Jsoup.clean(_, Whitelist.basic())),
    )

    if (isNewPodcast) {

      // experimental: this works but has terrible performance and assumes we have a GUI app
      // Option(p.getItunesImage).foreach(img => {
      //     p.setItunesImage(base64Image(img))
      // })
      p.image.foreach { img =>
        crawler ! DownloadWithHeadCheck(podcastId, img, PodcastImageFetchJob())
      }

      val indexEvent = AddDocIndexEvent(IndexMapper.toIndexDoc(p)) // AddDocIndexEvent(indexMapper.toImmutable(p))
      //emitIndexEvent(indexEvent)
      index ! indexEvent

      // request that the podcasts website will get added to the index as well, if possible
      p.link match {
        case Some(link) => crawler ! DownloadWithHeadCheck(p.id.get, link, WebsiteFetchJob())
        case None       => log.debug("No link set for podcast {} --> no website data will be added to the index", p.id.get)
      }
    }

    // we always update a podcasts metadata, this likely may have changed (new descriptions, etc)
    val catalogEvent = UpdatePodcast(podcastId, feedUrl, p)
    //emitCatalogEvent(catalogEvent)
    catalog ! catalogEvent

    // check for "new" episodes: because this is a new OldPodcast, all episodes will be new and registered
    for (e <- parser.episodes) {
      registerEpisode(podcastId, e)
    }

  }

  private def registerEpisode(podcastId: String, e: Episode): Unit = {

    // cleanup some potentially markuped texts
    val episode = e.copy(
      title          = e.title.map(_.trim),
      description    = e.description.map(Jsoup.clean(_, Whitelist.basic())),
      contentEncoded = e.contentEncoded.map(Jsoup.clean(_, Whitelist.basic()))
    )

    val catalogCommand = RegisterEpisodeIfNew(podcastId, episode)
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
