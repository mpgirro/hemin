package hemin.engine.parser

import java.util.Base64

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.Logger
import hemin.engine.catalog.CatalogStore._
import hemin.engine.crawler.Crawler.{DownloadWithHeadCheck, WebsiteFetchJob}
import hemin.engine.index.IndexStore.{AddDocIndexEvent, UpdateDocWebsiteDataIndexEvent}
import hemin.engine.model.{Episode, FeedStatus, Image, Podcast}
import hemin.engine.node.Node._
import hemin.engine.parser.Parser._
import hemin.engine.parser.feed.{FeedParser, RomeFeedParser}
import hemin.engine.parser.opml.{OpmlParser, RomeOpmlParser}
import hemin.engine.util.mapper.IndexMapper
import hemin.engine.util.{HashUtil, TimeUtil}
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Parser {
  final val name = "parser"
  def props(config: ParserConfig): Props =
    Props(new Parser(config))
      .withDispatcher(config.dispatcher)
      .withMailbox(config.mailbox)

  trait ParserMessage
  final case class ParseNewPodcastData(feedUrl: String, podcastId: String, feedData: String) extends ParserMessage
  final case class ParseUpdateEpisodeData(feedUrl: String, podcastId: String, episodeFeedData: String) extends ParserMessage
  final case class ParseWebsiteData(id: String, html: String) extends ParserMessage
  final case class ParseFyydEpisodes(podcastId: String, episodesData: String) extends ParserMessage
  final case class ParseImage(url: String, mime: Option[String], encoding: String, bytes: Array[Byte]) extends ParserMessage
  final case class ParseOpml(xmlData: String) extends ParserMessage
}

class   Parser (config: ParserConfig)
  extends Actor {

  private val log: Logger = Logger(getClass)

  log.debug("{} running on dispatcher : {}", self.path.name, context.system.dispatchers.lookup(context.props.dispatcher))
  log.debug("{} running with mailbox : {}", self.path.name, context.system.mailboxes.lookup(context.props.mailbox))

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private val feedParser: FeedParser = new RomeFeedParser
  private val opmlParser: OpmlParser = new RomeOpmlParser

  private var catalog: ActorRef = _
  private var index: ActorRef = _
  private var crawler: ActorRef = _
  private var supervisor: ActorRef = _

  override def postRestart(cause: Throwable): Unit = {
    log.warn("{} has been restarted or resumed", self.path.name)
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
      supervisor ! ReportParserInitializationComplete

    case ParseNewPodcastData(feedUrl, podcastId, feedData) => onParseNewPodcastData(feedUrl, podcastId, feedData)

    case ParseUpdateEpisodeData(feedUrl, podcastId, episodeFeedData) => onParseUpdateEpisodeData(feedUrl, podcastId, episodeFeedData)

    case ParseWebsiteData(id, html) => onParseWebsiteData(id, html)

    case ParseFyydEpisodes(podcastId, json) => onParseFyydEpisodes(podcastId, json)

    case ParseImage(url, mime, encoding, bytes) => onParseImage(url, mime, encoding, bytes)

    case ParseOpml(xmlData) => onParseOpml(xmlData)

  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
  }

  private def onParseNewPodcastData(feedUrl: String, podcastId: String, feedData: String): Unit = {
    log.debug("Received ParseNewPodcastData for feed: " + feedUrl)
    parseFeedData(podcastId, feedUrl, feedData, isNewPodcast = true)
  }

  private def onParseUpdateEpisodeData(feedUrl: String, podcastId: String, episodeFeedData: String): Unit = {
    log.debug("Received ParseEpisodeData({},{},_)", feedUrl, podcastId)
    parseFeedData(podcastId, feedUrl, episodeFeedData, isNewPodcast = false)
  }

  private def parseFeedData(podcastId: String, feedUrl: String, feedData: String, isNewPodcast: Boolean): Unit = Future {
    feedParser.parse(feedData) match {
      case Success(result) =>
        val p: Podcast = result.podcast.copy(
          id = Some(podcastId),
          title = result.podcast.title.map(_.trim),
          description = result.podcast.description.map(Jsoup.clean(_, Whitelist.basic())),
        )

        if (isNewPodcast) {

          IndexMapper.toDocument(p) match {
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
            case None => log.debug("No link set for podcast {} --> no website data will be added to the index", p.id.get)
          }
        }

        // we always update a podcasts metadata, this likely may have changed (new descriptions, etc)
        val catalogEvent = UpdatePodcast(podcastId, feedUrl, p)
        //emitCatalogEvent(catalogEvent)
        catalog ! catalogEvent

        // check for "new" episodes: because this is a new OldPodcast, all episodes will be new and registered
        result.episodes.foreach(e => registerEpisode(podcastId, e))

      case Failure(ex) =>
        log.error("Error creating a parser for the feed '{}' ; reason : {}", feedUrl, ex.getMessage)
        ex.printStackTrace()

        // we update the status of the feed, to persist the information that this feed stinks
        val catalogEvent = FeedStatusUpdate(podcastId, feedUrl, TimeUtil.now, FeedStatus.ParserError)
        //emitCatalogEvent(catalogEvent)
        catalog ! catalogEvent
    }
  }

  private def registerEpisode(podcastId: String, e: Episode): Unit = {

    // cleanup some potentially markuped texts
    val episode = e.copy(
      title = e.title.map(_.trim),
      description = e.description.map(Jsoup.clean(_, Whitelist.basic())),
      contentEncoded = e.contentEncoded.map(Jsoup.clean(_, Whitelist.basic())),
    )

    val catalogCommand = RegisterEpisodeIfNew(podcastId, episode)
    //sendCatalogCommand(catalogCommand)
    catalog ! catalogCommand
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

  private def onParseImage(url: String, mime: Option[String], encoding: String, bytes: Array[Byte]): Unit = {
    log.debug("Received ParseImage({})", url)
    imageFromBytes(url, mime, encoding, bytes)
      .foreach { image =>
        catalog ! UpdateImage(image)
      }
  }

  private def imageFromBytes(url: String, mime: Option[String], encoding: String, bytes: Array[Byte]): Future[Image] = Future {
    val image = com.sksamuel.scrimage.Image.apply(bytes)
    val data = transform(image)

    // TODO set more fields of following instance!
    Image(
      url  = Some(url),
      data = Some(base64(data, mime, encoding)),
      hash = Some(HashUtil.sha1(data)),
    )
  }

  private def transform(image: com.sksamuel.scrimage.Image): Array[Byte] = image
    .cover(500, 500)
    .bound(500, 500)
    .bytes

  private def base64(bytes: Array[Byte], mimeType: Option[String], encoding: String): String = {
    val mime: String = mimeType.map(_ + ";").getOrElse("")
    val base64: String = Base64.getEncoder.encodeToString(bytes)
    s"data:${mime}charset=$encoding;base64,$base64"
  }

  /*
  private def sendCatalogCommand(command: CatalogCommand): Unit = {
      mediator ! Send("/user/node/"+CatalogStore.name, command, localAffinity = true)
  }

  private def emitCatalogEvent(event: CatalogEvent): Unit = {
      mediator ! Publish(catalogEventStream, event)
  }

  private def emitIndexEvent(event: IndexEvent): Unit = {
      mediator ! Publish(indexEventStream, event)
  }
  */

  private def onParseOpml(xmlData: String): Unit = Future {
    log.debug("Received ParseOpml(_)")
    opmlParser.parse(xmlData) match {
      case Success(result) =>
        result.feedUrls.foreach(f => catalog ! ProposeNewFeed(f))
      case Failure(ex) =>
        log.error("Error creating a parser for the OPML file ; reason : {}", ex.getMessage)
        ex.printStackTrace()
    }
  }

}
