package io.hemin.engine.parser

import java.io.{ByteArrayInputStream, File, InputStream, PrintWriter}
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.hemin.engine.catalog.CatalogStore._
import io.hemin.engine.crawler.Crawler.{DownloadWithHeadCheck, PodcastImageFetchJob, WebsiteFetchJob}
import io.hemin.engine.index.IndexStore.{AddDocIndexEvent, UpdateDocWebsiteDataIndexEvent}
import io.hemin.engine.model._
import io.hemin.engine.node.Node._
import io.hemin.engine.parser.Parser._
import io.hemin.engine.parser.feed.RomeFeedParser
import io.hemin.engine.util.HashUtil
import io.hemin.engine.util.mapper.IndexMapper
import javax.imageio.ImageIO
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object ParserWorker {
  def name(workerIndex: Int): String = "worker-" + workerIndex
  def props(config: ParserConfig): Props =
    Props(new ParserWorker(config))
      .withDispatcher(config.dispatcher)
      .withMailbox(config.mailbox)
}

class ParserWorker (config: ParserConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher : {}", self.path.name, context.system.dispatchers.lookup(context.props.dispatcher))
  log.debug("{} running with mailbox : {}", self.path.name, context.system.mailboxes.lookup(context.props.mailbox))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private var catalog: ActorRef = _
  private var index: ActorRef = _
  private var crawler: ActorRef = _
  private var supervisor: ActorRef = _

  override def postRestart(cause: Throwable): Unit = {
    log.warning("{} has been restarted or resumed", self.path.name)
    cause match {
      case e: Exception =>
        log.error("Unhandled Exception : {}", e.getMessage, e)
    }
    super.postRestart(cause)
  }

  override def postStop: Unit = {
    log.debug("shutting down")
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

  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
  }

  private def onParseNewPodcastData(feedUrl: String, podcastId: String, feedData: String): Unit = {
    log.debug("Received ParseNewPodcastData for feed: " + feedUrl)
    parse(podcastId, feedUrl, feedData, isNewPodcast = true)
  }

  private def onParseUpdateEpisodeData(feedUrl: String, podcastId: String, episodeFeedData: String): Unit = {
    log.debug("Received ParseEpisodeData({},{},_)", feedUrl, podcastId)
    parse(podcastId, feedUrl, episodeFeedData, isNewPodcast = false)
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

    val imgBytes: Array[Byte] = imageData.getBytes(StandardCharsets.UTF_8.name)
    val imgFile: File = strToFile(imageData)
    //val imgFile: File = bytesToFile(imgBytes)

    val image = com.sksamuel.scrimage.Image.fromFile(imgFile)
    //val image = com.sksamuel.scrimage.Image.apply(imgBytes)
    //val image = com.sksamuel.scrimage.Image.fromStream(inputStreamFromString(imageData))
    val data = transform(image)

    // TODO transform data to base64?

    // TODO set more fields of following instance!
    Image(
      associateId = Some(associateId),
      data        = Some(data),
      hash        = Some(HashUtil.sha1(data)),
    )
  }

  private def strToFile(imageStr: String): File = {

    val path = "/Users/max/Desktop/img_tmp"

    val out = new PrintWriter(path)
    out.println(imageStr)
    out.close()

    new File(path)
  }

  private def bytesToFile(imageByteArray: Array[Byte]): File = {
    import java.io.FileOutputStream

    val path = "/Users/max/Desktop/img_tmp"

    val imageOutFile = new FileOutputStream(path)

    imageOutFile.write(imageByteArray)
    imageOutFile.close()

    new File(path)
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

    RomeFeedParser.parse(feedData) match {
      case Success(parser) =>
        val p: Podcast = parser.podcast.copy(
          id          = Some(podcastId),
          title       = parser.podcast.title.map(_.trim),
          description = parser.podcast.description.map(Jsoup.clean(_, Whitelist.basic())),
        )

        if (isNewPodcast) {

          IndexMapper.toIndexDoc(p) match {
            case Success(doc) =>
              val indexEvent = AddDocIndexEvent(doc)
              //emitIndexEvent(indexEvent)
              index ! indexEvent
            case Failure(ex) =>
              log.error("Failed to map Podcast to IndexDoc; reason : {}", ex.getMessage)
              ex.printStackTrace()
          }

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

      case Failure(ex) =>
        log.error("Error creating a parser for the feed '{}' ; reason : {}", feedUrl, ex.getMessage)
        ex.printStackTrace()

        // we update the status of the feed, to persist the information that this feed stinks
        val catalogEvent = FeedStatusUpdate(podcastId, feedUrl, LocalDateTime.now(), FeedStatus.ParserError)
        //emitCatalogEvent(catalogEvent)
        catalog ! catalogEvent
    }
  }

  private def registerEpisode(podcastId: String, e: Episode): Unit = {

    // cleanup some potentially markuped texts
    val episode = e.copy(
      title          = e.title.map(_.trim),
      description    = e.description.map(Jsoup.clean(_, Whitelist.basic())),
      contentEncoded = e.contentEncoded.map(Jsoup.clean(_, Whitelist.basic())),
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
