package io.disposia.engine.catalog

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.disposia.engine.EngineProtocol._
import io.disposia.engine.catalog.CatalogStore._
import io.disposia.engine.catalog.repository.{EpisodeRepository, FeedRepository, PodcastRepository}
import io.disposia.engine.crawler.Crawler.{NewPodcastFetchJob, UpdateEpisodesFetchJob, WebsiteFetchJob}
import io.disposia.engine.domain._
import io.disposia.engine.index.IndexStore.AddDocIndexEvent
import io.disposia.engine.mapper._
import io.disposia.engine.updater.Updater.ProcessFeed
import io.disposia.engine.util.ExoGenerator
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

object CatalogStore {
    final val name = "catalog"
    def props(config: CatalogConfig): Props = {
        Props(new CatalogStore(config)).withDispatcher("echo.catalog.dispatcher")
    }

    trait CatalogMessage
    trait CatalogEvent extends CatalogMessage
    trait CatalogCommand extends CatalogMessage
    trait CatalogQuery extends CatalogMessage
    trait CatalogQueryResult extends CatalogMessage
    // CatalogCommands
    case class ProposeNewFeed(url: String) extends CatalogCommand                 // Web/CLI -> CatalogStore
    case class RegisterEpisodeIfNew(podcastExo: String, episode: Episode) extends CatalogCommand // Questions: Parser -> CatalogStore
    // CatalogEvents
    //case class AddPodcastAndFeedIfUnknown(podcast: Podcast, feed: Feed) extends CatalogEvent
    case class FeedStatusUpdate(podcastExo: String, feedUrl: String, timestamp: LocalDateTime, status: FeedStatus) extends CatalogEvent
    case class UpdateFeedUrl(oldUrl: String, newUrl: String) extends CatalogEvent
    case class UpdateLinkByExo(exo: String, newUrl: String) extends CatalogEvent
    case class SaveChapter(chapter: Chapter) extends CatalogEvent
    case class UpdatePodcast(podcastExo: String, feedUrl: String, podcast: Podcast) extends CatalogEvent
    case class UpdateEpisode(episode: Episode) extends CatalogEvent
    case class UpdateEpisodeWithChapters(podcastExo: String, episode: Episode, chapter: List[Chapter]) extends CatalogEvent
    // CatalogQueries
    case class GetPodcast(exo: String) extends CatalogQuery
    case class GetAllPodcasts(page: Int, size: Int) extends CatalogQuery
    case class GetAllPodcastsRegistrationComplete(page: Int, size: Int) extends CatalogQuery
    case class GetAllFeeds(page: Int, size: Int) extends CatalogQuery
    case class GetEpisode(exo: String) extends CatalogQuery
    case class GetEpisodesByPodcast(podcastExo: String) extends CatalogQuery
    case class GetFeedsByPodcast(podcastExo: String) extends CatalogQuery
    case class GetChaptersByEpisode(episodeExo: String) extends CatalogQuery
    case class GetFeed(exo: String) extends CatalogQuery
    case class CheckPodcast(exo: String) extends CatalogQuery
    case class CheckFeed(exo: String) extends CatalogQuery
    //case class CheckAllPodcasts() extends CatalogQuery
    case class CheckAllFeeds() extends CatalogQuery
    // CatalogQueryResults
    case class PodcastResult(podcast: Option[Podcast]) extends CatalogQueryResult
    case class AllPodcastsResult(results: List[Podcast]) extends CatalogQueryResult
    case class AllFeedsResult(results: List[Feed]) extends CatalogQueryResult
    case class EpisodeResult(episode: Option[Episode]) extends CatalogQueryResult                      // TODO make it an option, and remove NothingFound message
    case class EpisodesByPodcastResult(episodes: List[Episode]) extends CatalogQueryResult
    case class FeedsByPodcastResult(feeds: List[Feed]) extends CatalogQueryResult
    case class ChaptersByEpisodeResult(chapters: List[Chapter]) extends CatalogQueryResult
    case class FeedResult(feed: Option[Feed]) extends CatalogQueryResult
    //case class NothingFound(exo: String) extends CatalogQueryResult
}

class CatalogStore(config: CatalogConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

  private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.catalog.dispatcher")

  private val exoGenerator: ExoGenerator = new ExoGenerator(1) // TODO get shardId from Config

  private var catalogStore: ActorRef = _
  private var indexStore: ActorRef = _
  private var crawler: ActorRef = _
  private var updater: ActorRef = _
  private var supervisor: ActorRef = _

  private lazy val (connection, dbName) = {
    val driver = MongoDriver()

    //registerDriverShutdownHook(driver)

    (for {
      parsedUri <- MongoConnection.parseURI(config.mongoUri)
      con <- driver.connection(parsedUri, strictUri = true)
      db <- parsedUri.db match {
        case Some(dbName) => Success(dbName)
        case _            => Failure[String](new IllegalArgumentException(
          s"cannot resolve connection from URI: $parsedUri"
        ))
      }
    } yield con -> db).get
  }

  private lazy val lnm = s"${connection.supervisor}/${connection.name}"

  @inline private def resolveDB(ec: ExecutionContext) =
    connection.database(dbName)(ec).andThen {
      case _ => /*logger.debug*/ log.info(s"[$lnm] MongoDB resolved: $dbName")
    }

  private def db(implicit ec: ExecutionContext): DefaultDB = Await.result(resolveDB(ec), 10.seconds)

  private val podcasts: PodcastRepository = new PodcastRepository(db, executionContext)
  private val episodes: EpisodeRepository = new EpisodeRepository(db, executionContext)
  private val feeds: FeedRepository = new FeedRepository(db, executionContext)
  //private val chapters: ChapterRepository = new ChapterRepository(db, executionContext)

  /* TODO
   private def registerDriverShutdownHook(mongoDriver: MongoDriver): Unit =
      lifecycle.addStopHook { () =>
        logger.info(s"[$lnm] Stopping the MongoDriver...")
        Future(mongoDriver.close())
      }
   */

  private val podcastMapper = PodcastMapper.INSTANCE
  private val episodeMapper = EpisodeMapper.INSTANCE
  private val feedMapper = FeedMapper.INSTANCE
  private val chapterMapper = ChapterMapper.INSTANCE
  private val indexMapper = IndexMapper.INSTANCE
  private val idMapper = IdMapper.INSTANCE

  override def postRestart(cause: Throwable): Unit = {
    log.warning("{} has been restarted or resumed", self.path.name)

    //log.info(s"[$lnm] Stopping the MongoDriver...")
    //Future(mongoDriver.close())

    super.postRestart(cause)
  }

  override def postStop(): Unit = {
    log.info("shutting down")
  }

  override def receive: Receive = {

    case ActorRefCatalogStoreActor(ref) =>
      log.debug("Received ActorRefCatalogStoreActor(_)")
      catalogStore = ref

    case ActorRefIndexStoreActor(ref) =>
      log.debug("Received ActorRefIndexStoreActor(_)")
      indexStore = ref

    case ActorRefCrawlerActor(ref) =>
      log.debug("Received ActorRefCrawlerActor(_)")
      crawler = ref

    case ActorRefUpdaterActor(ref) =>
      log.debug("Received ActorRefUpdaterActor(_)")
      updater = ref

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      supervisor ! ReportCatalogStoreStartupComplete

    case ProposeNewFeed(feedUrl) => proposeFeed(feedUrl)

    case CheckPodcast(exo) => onCheckPodcast(exo)

    case CheckFeed(exo) => onCheckFeed(exo)

    //case CheckAllPodcasts => onCheckAllPodcasts(0, config.maxPageSize)

    case CheckAllFeeds => onCheckAllFeeds(0, config.maxPageSize)

    case FeedStatusUpdate(podcastExo, feedUrl, timestamp, status) => onFeedStatusUpdate(podcastExo, feedUrl, timestamp, status)

    //case SaveChapter(chapter) => onSaveChapter(chapter)

    //case AddPodcastAndFeedIfUnknown(podcast, feed) => onAddPodcastAndFeedIfUnknown(podcast, feed)

    case UpdatePodcast(exo, url, podcast) => onUpdatePodcast(exo, url, podcast)

    case UpdateEpisode(episode) => onUpdateEpisode(episode)

    // TODO
    //case UpdateFeed(podcastExo, feed) =>  ...
    //case UpdateChapter(episodeExo, chapter) =>  ...

    case UpdateFeedUrl(oldUrl, newUrl) => onUpdateFeedMetadataUrl(oldUrl, newUrl)

    case UpdateLinkByExo(exo, newUrl) => onUpdateLinkByExo(exo, newUrl)

    case GetPodcast(exo) => onGetPodcast(exo)

    case GetAllPodcasts(page, size) => onGetAllPodcasts(page, size)

    case GetAllPodcastsRegistrationComplete(page, size) => onGetAllPodcastsRegistrationComplete(page, size)

    case GetAllFeeds(page, size) => onGetAllFeeds(page, size)

    case GetEpisode(exo) => onGetEpisode(exo)

    case GetEpisodesByPodcast(podcastExo) => onGetEpisodesByPodcast(podcastExo)

    case GetFeedsByPodcast(podcastExo) => onGetFeedsByPodcast(podcastExo)

    case GetChaptersByEpisode(episodeExo) => onGetChaptersByEpisode(episodeExo)

    case GetFeed(exo) => onGetFeed(exo)

    case RegisterEpisodeIfNew(podcastExo, episode) => onRegisterEpisodeIfNew(podcastExo, episode)

    case DebugPrintAllPodcasts => debugPrintAllPodcasts()

    case DebugPrintAllEpisodes => debugPrintAllEpisodes()

    case DebugPrintAllFeeds => debugPrintAllFeeds()

    case unhandled => log.warning("Received message of unhandeled type : {}", unhandled.getClass)

  }

  /*
  private def emitCatalogEvent(event: CatalogEvent): Unit = {
      mediator ! Publish(catalogEventStream, event)
  }

  private def emitIndexEvent(event: IndexEvent): Unit = {
      mediator ! Publish(indexEventStream, event)
  }
  */

  private def onError(msg: String, ex: Throwable): Unit = {
    log.error("{} : {}", msg, ex.getMessage)
    ex.printStackTrace()
  }

  private def proposeFeed(url: String): Unit = {
    log.info("Received msg proposing a new feed: " + url)

    feeds
      .findAllByUrl(url)
      .onComplete {
        case Success(fs) =>
          if (fs.isEmpty) {
            val podcastExo = exoGenerator.getNewExo
            var podcast = ImmutablePodcast.builder()
              .setExo(podcastExo)
              .setTitle(podcastExo)
              .setDescription(url)
              .setRegistrationComplete(false)
              .setRegistrationTimestamp(LocalDateTime.now())
              .create()
            val feedExo = exoGenerator.getNewExo
            val feed = ImmutableFeed.builder()
              .setExo(feedExo)
              .setPodcastExo(podcastExo)
              .setUrl(url)
              .setLastChecked(LocalDateTime.now())
              .setLastStatus(FeedStatus.NEVER_CHECKED)
              .setRegistrationTimestamp(LocalDateTime.now())
              .create()

            // Note: we chain the create calls to ensure that the
            // message to the Updater is only dispatched once we can
            // be sure the podcast and the feed are in the database
            podcasts
              .save(podcast)
              .foreach(_ => {
                feeds
                  .save(feed)
                  .foreach(_ => {
                    /*
                    val catalogEvent = AddPodcastAndFeedIfUnknown(
                      idMapper.clearImmutable(podcast),
                      idMapper.clearImmutable(feed))
                    //emitCatalogEvent(catalogEvent)
                    self ! catalogEvent
                    */
                    updater ! ProcessFeed(podcastExo, url, NewPodcastFetchJob())
                  })
              })
          } else {
            log.info("Feed URL is already in database : {}", url)
          }
        case Failure(ex) => onError("Could not get all feeds by URL="+url, ex)
      }

  }

  private def onFeedStatusUpdate(podcastExo: String, url: String, timestamp: LocalDateTime, status: FeedStatus): Unit = {
    log.debug("Received FeedStatusUpdate({},{},{})", url, timestamp, status)

    feeds
      .findOneByUrlAndPodcastExo(url, podcastExo)
      .foreach {
        case Some(f) =>
          feeds.save(feedMapper
            .toImmutable(f)
            .withLastChecked(timestamp)
            .withLastStatus(status))
        case None => log.warning("No Feed found for Podcast (EXO:{}) and URL : {}", podcastExo, url)
      }
  }

  @Deprecated
  private def onUpdatePodcast(podcastExo: String, feedUrl: String, podcast: Podcast): Unit = {
    log.debug("Received UpdatePodcast({},{},{})", podcastExo, feedUrl, podcast.getExo)

    /* TODO
     * hier empfange ich die feedUrl die mir der Parser zurückgib, um anschließend die episode laden zu können
     * das würde ich mir gerne ersparen. dazu müsste ich aus der DB den "primärfeed" irgednwie bekommen können, also
     * jenen feed den ich immer benutze um updates zu laden
     */

    podcasts
      .findOne(podcastExo)
      .map {
        case Some(p) => podcastMapper.update(podcast, p)
        case None =>
          log.debug("Podcast to update is not yet in database, therefore it will be added : {}", podcast.getExo)
          podcastMapper.toModifiable(podcast)
      }
      .foreach(p => {
        p.setRegistrationComplete(true)
        //podcastService.save(p)
        podcasts.save(p)

        // TODO we will fetch feeds for checking new episodes, but not because we updated podcast metadata
        // crawler ! FetchFeedForUpdateEpisodes(feedUrl, podcastId)
      })
  }

  private def onUpdateEpisode(episode: Episode): Unit = {
    log.debug("Received UpdateEpisode({})", episode.getExo)

    episodes
      .findOne(episode.getExo)
      .map {
        case Some(e) => episodeMapper.update(episode, e)
        case None =>
          log.debug("Episode to update is not yet in database, therefore it will be added : {}", episode.getExo)
          episodeMapper.toModifiable(episode)
      }
      .foreach(e => {
        episodes.save(e)
      })
  }


  private def onUpdateFeedMetadataUrl(oldUrl: String, newUrl: String): Unit = {
    log.debug("Received UpdateFeedUrl('{}','{}')", oldUrl, newUrl)

    feeds
      .findAllByUrl(oldUrl)
      .onComplete {
        case Success(fs) =>
          if (fs.nonEmpty) {
            fs.foreach(f => {
              feeds.save(feedMapper
                .toImmutable(f)
                .withUrl(newUrl))
            })
          } else {
            log.error("No Feed found in database with url='{}'", oldUrl)
          }
        case Failure(ex) => onError("Could not get all feeds by URL="+oldUrl, ex)
      }
  }

  private def onUpdateLinkByExo(exo: String, newUrl: String): Unit = {
    log.debug("Received UpdateLinkByExo({},'{}')", exo, newUrl)

    podcasts
      .findOne(exo)
      .foreach {
        case Some(p) => podcasts.save(podcastMapper.toImmutable(p).withLink(newUrl))
        case None =>
          episodes
            .findOne(exo)
            .foreach {
              case Some(e) => episodes.save(episodeMapper.toImmutable(e).withLink(newUrl))
              case None    => log.error("Cannot update Link URL - no Podcast or Episode found by EXO : {}", exo)
            }
      }
  }

  private def onGetPodcast(exo: String): Unit = {
    log.debug("Received GetPodcast('{}')", exo)

    val theSender = sender()
    podcasts
      .findOne(exo)
      .andThen {
        case Success(p)  => p
        case Failure(ex) =>
          onError(s"Error on retrieving Podcast (EXO=$exo) from database : ", ex)
          None // we have no results to return
      }
      .map { p =>
        theSender ! PodcastResult(p)
      }
  }

  private def onGetAllPodcasts(page: Int, size: Int): Unit = {
    log.debug("Received GetAllPodcasts({},{})", page, size)

    val theSender = sender()
    podcasts
      .findAll(page, size)
      .andThen {
        case Success(ps) => ps
        case Failure(ex) =>
          onError(s"Could not get all Podcasts by page=$page and size=$size", ex)
          List() // we have no results to return
      }
      .map { ps =>
        theSender ! AllPodcastsResult(ps)
      }
  }

  private def onGetAllPodcastsRegistrationComplete(page: Int, size: Int): Unit = {
    log.debug("Received GetAllPodcastsRegistrationComplete({},{})", page, size)

    val theSender = sender()
    podcasts
      .findAllRegistrationCompleteAsTeaser(page, size)
      .andThen {
        case Success(ps) => ps
        case Failure(ex) =>
          onError(s"Could not get all Podcasts by page=$page and size=$size and registrationCompelete=TRUE", ex)
          List() // we have no results to return
      }
      .map { ps =>
        theSender ! AllPodcastsResult(ps)
      }
  }

  private def onGetAllFeeds(page: Int, size: Int): Unit = {
    log.debug("Received GetAllFeeds({},{})", page, size)

    val theSender = sender()
    feeds
      .findAll(page, size)
      .andThen {
        case Success(fs) => fs
        case Failure(ex) =>
          onError(s"Could not get all Feeds by page=$page and size=$size", ex)
          List() // we have no results to return
      }
      .map { fs =>
        theSender ! AllFeedsResult(fs)
      }
  }

  private def onGetEpisode(exo: String): Unit= {
    log.debug("Received GetEpisode('{}')", exo)

    val theSender = sender()
    episodes
      .findOne(exo)
      .andThen {
        case Success(e)  => e
        case Failure(ex) =>
          onError(s"Error on retrieving Episode (EXO=$exo) from database : ", ex)
          None // we have no results to return
      }
      .map { e =>
        theSender ! EpisodeResult(e)
      }
  }

  private def onGetEpisodesByPodcast(podcastId: String): Unit = {
    log.debug("Received GetEpisodesByPodcast('{}')", podcastId)

    val theSender = sender()
    episodes
      .findAllByPodcast(podcastId)
      .andThen {
        case Success(es) => es
        case Failure(ex) =>
          onError(s"Could not get all Episodes by Podcast (ID=$podcastId)", ex)
          List() // we have no results to return
      }
      .map { es =>
        theSender ! EpisodesByPodcastResult(es)
      }
  }

  private def onGetFeedsByPodcast(podcastId: String): Unit = {
    log.debug("Received GetFeedsByPodcast('{}')", podcastId)

    val theSender = sender()
    feeds
      .findAllByPodcast(podcastId)
      .andThen {
        case Success(fs) => fs
        case Failure(ex) =>
          onError(s"Could not get all Feeds by Podcast (ID=$podcastId)", ex)
          List() // we have no results to return
      }
      .map { fs =>
        theSender ! FeedsByPodcastResult(fs)
      }
  }

  private def onGetChaptersByEpisode(episodeId: String): Unit = {
    log.debug("Received GetChaptersByEpisode('{}')", episodeId)

    val theSender = sender()
    episodes
      .findOne(episodeId)
      .map {
        case Some(e) =>
          Option(e.getChapters) match {
            case Some(cs) => cs.asScala.toList
            case None     => List()
          }
        case None =>
          log.warning("Database does not contain Episode (EXO) : {}", episodeId)
          List()
      }
      .foreach { cs => theSender ! ChaptersByEpisodeResult(cs) }
  }

  private def onGetFeed(exo: String): Unit = {
    log.debug("Received GetFeed('{}')", exo)

    val theSender = sender()
    feeds
      .findOne(exo)
      .andThen {
        case Success(f)  => f
        case Failure(ex) =>
          onError(s"Error on retrieving Feed (EXO=$exo) from database : ", ex)
          None // we have no results to return
      }
      .map { f =>
        theSender ! FeedResult(f)
      }
  }

  private def onCheckPodcast(podcastId: String): Unit = {
    log.debug("Received CheckPodcast({})", podcastId)

    feeds
      .findAllByPodcast(podcastId)
      .onComplete {
        case Success(fs) =>
          if (fs.nonEmpty) {
            val f = fs.head
            updater ! ProcessFeed(f.getPodcastExo, f.getUrl, UpdateEpisodesFetchJob(null, null))
          } else {
            log.error(s"Chould not update Podcast (EXO = $podcastId) -- No feed found")
          }
        case Failure(ex) => log.error(s"Could not retrieve Feeds by Podcast (EXO = $podcastId) : {}", ex)
      }
  }

  private def onCheckFeed(feedId: String): Unit = {
    log.debug("Received CheckFeed({})", feedId)

    feeds
      .findOne(feedId)
      .foreach {
        case Some(f) => updater ! ProcessFeed(f.getPodcastExo, f.getUrl, UpdateEpisodesFetchJob(null, null))
        case None    => log.error("No Feed in Database (EXO) : {}", feedId)
      }
  }

  private def onCheckAllFeeds(page: Int, size: Int): Unit = {
    log.debug("Received CheckAllFeeds({},{})", page, size)

    // TODO
    // we need a new way to update feeds. Running an update on all Feeds isn't practical

    throw new UnsupportedOperationException("Currently not implemented")
  }

  private def onRegisterEpisodeIfNew(podcastExo: String, episode: Episode): Unit = {
    log.debug("Received RegisterEpisodeIfNew({}, '{}')", podcastExo, episode.getTitle)

    podcasts
      .findOne(podcastExo)
      .foreach {
        case Some(p) =>
          Option(episode.getGuid)
            .map(guid => {
              episodes
                .findAllByPodcastAndGuid(podcastExo, guid)
                .map(es => es.headOption)
            })
            .getOrElse({
              episodes
                .findOneByEnclosure(episode.getEnclosureUrl, episode.getEnclosureLength, episode.getEnclosureType)
            })
            .map {
              case Some(e) => log.debug("Episode is already registered : ('{}', {}, '{}')",episode.getEnclosureUrl, episode.getEnclosureLength, episode.getEnclosureType)
              case None =>
                val e = episodeMapper.toModifiable(episode)

                // generate a new episode exo - the generator is (almost) ensuring uniqueness
                e.setExo(exoGenerator.getNewExo)
                e.setPodcastExo(podcastExo)
                e.setPodcastTitle(p.getTitle) // we'll not re-use this DTO, but extract the info again a bit further down
                e.setRegistrationTimestamp(LocalDateTime.now())

                // check if the episode has a cover image defined, and set the one of the episode
                Option(e.getImage).getOrElse({
                  e.setImage(p.getImage)
                })

                // save asynchronously
                episodes
                  .save(e)
                  .onComplete {
                    case Success(_) =>
                      log.info("episode registered : '{}' [p:{},e:{}]", e.getTitle, podcastExo, e.getExo)

                      val indexEvent = AddDocIndexEvent(indexMapper.toImmutable(e))
                      //emitIndexEvent(indexEvent)
                      indexStore ! indexEvent

                      /* TODO send an update to all catalogs via the broker, so all other stores will have
                       * the data too (this will of course mean that I will update my own data, which is a
                       * bit pointless, by oh well... */
                      //val catalogEvent = UpdateEpisode(podcastExo, idMapper.clearImmutable(e))
                      //emitCatalogEvent(catalogEvent)
                      //self ! catalogEvent

                      // request that the website will get added to the episodes index entry as well
                      Option(e.getLink) match {
                        case Some(url) => updater ! ProcessFeed(e.getExo, url, WebsiteFetchJob())
                        case None      => log.debug("No link set for episode {} --> no website data will be added to the index", episode.getExo)
                      }
                    case Failure(ex) => onError("Could not save new Episode", ex)
                  }

              /* TODO in case I have a bug, I need to adjust the title
              val result = episodeService.save(e)

              // we already clean up all the IDs here, just for good manners. for the chapters,
              // we simply reuse the chapters from since bevore saving the episode, because those yet lack an ID
              result
                .map(r => episodeMapper.toImmutable(r)
                  .withPodcastTitle(e.getPodcastTitle))
                .map(r => idMapper.clearImmutable(r)
                  .withChapters(Option(e.getChapters)
                    .map(_
                      .asScala
                      .map(c => idMapper.clearImmutable(c))
                      .asJava)
                    .orNull))
                    */
            }
        case None => log.error("Could not register Episode -- No  parent Podcast for (EXO) : {}", podcastExo)
      }
  }

  private def debugPrintAllPodcasts(): Unit = {
    log.debug("Received DebugPrintAllPodcasts")
    log.info("All Podcasts in database:")

    podcasts
      .findAll(0, config.maxPageSize)
      .onComplete {
        case Success(ps) => ps.foreach(p => println(s"${p.getExo} : ${p.getTitle}"))
        case Failure(ex) => log.error("Could not retrieve and print all Podcasts : {}", ex)
      }
  }

  private def debugPrintAllEpisodes(): Unit = {
    log.debug("Received DebugPrintAllEpisodes")
    log.info("All Episodes in database:")

    episodes
      .findAll(0, config.maxPageSize)
      .onComplete {
        case Success(es) => es.foreach(e => println(s"${e.getExo} : ${e.getTitle}"))
        case Failure(ex) => log.error("Could not retrieve and print all Episodes : {}", ex)
      }
  }

  private def debugPrintAllFeeds(): Unit = {
    log.debug("Received DebugPrintAllFeeds")
    log.info("All Feeds in database:")

    feeds
      .findAll(0, config.maxPageSize)
      .onComplete {
        case Success(fs) => fs.foreach(f => println(s"${f.getExo} : ${f.getUrl}"))
        case Failure(ex) => log.error("Could not retrieve and print all Feeds : {}", ex)
      }
  }

}
