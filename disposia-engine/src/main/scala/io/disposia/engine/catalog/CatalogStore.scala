package io.disposia.engine.catalog

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.disposia.engine.EngineProtocol._
import io.disposia.engine.catalog.CatalogStore._
import io.disposia.engine.catalog.repository.{EpisodeRepository, FeedRepository, PodcastRepository}
import io.disposia.engine.crawler.Crawler.{NewPodcastFetchJob, UpdateEpisodesFetchJob, WebsiteFetchJob}
import io.disposia.engine.domain.episode.EpisodeRegistrationInfo
import io.disposia.engine.domain.podcast.PodcastRegistrationInfo
import io.disposia.engine.domain._
import io.disposia.engine.index.IndexStore.AddDocIndexEvent
import io.disposia.engine.updater.Updater.ProcessFeed
import io.disposia.engine.util.IdGenerator
import io.disposia.engine.util.mapper.{IndexMapper, reduce}
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

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
    case class RegisterEpisodeIfNew(podcastId: String, episode: Episode) extends CatalogCommand // Questions: Parser -> CatalogStore
    // CatalogEvents
    //case class AddPodcastAndFeedIfUnknown(podcast: OldPodcast, feed: OldFeed) extends CatalogEvent
    case class FeedStatusUpdate(podcastId: String, feedUrl: String, timestamp: LocalDateTime, status: FeedStatus) extends CatalogEvent
    case class UpdateFeedUrl(oldUrl: String, newUrl: String) extends CatalogEvent
    case class UpdateLinkById(id: String, newUrl: String) extends CatalogEvent
    case class SaveChapter(chapter: Chapter) extends CatalogEvent
    case class UpdatePodcast(podcastId: String, feedUrl: String, podcast: Podcast) extends CatalogEvent
    case class UpdateEpisode(episode: Episode) extends CatalogEvent
    case class UpdateEpisodeWithChapters(podcastId: String, episode: Episode, chapter: List[Chapter]) extends CatalogEvent
    // CatalogQueries
    case class GetPodcast(id: String) extends CatalogQuery
    case class GetAllPodcasts(page: Int, size: Int) extends CatalogQuery
    case class GetAllPodcastsRegistrationComplete(page: Int, size: Int) extends CatalogQuery
    case class GetAllFeeds(page: Int, size: Int) extends CatalogQuery
    case class GetEpisode(id: String) extends CatalogQuery
    case class GetEpisodesByPodcast(podcastId: String) extends CatalogQuery
    case class GetFeedsByPodcast(podcastId: String) extends CatalogQuery
    case class GetChaptersByEpisode(episodeId: String) extends CatalogQuery
    case class GetFeed(id: String) extends CatalogQuery
    case class CheckPodcast(id: String) extends CatalogQuery
    case class CheckFeed(id: String) extends CatalogQuery
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

  private val idGenerator = new IdGenerator(1)  // TODO get shardId from Config

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

  // white all data if we please
  if (config.createDatabase) {
    log.info("Dropping database collections on startup")
    podcasts.drop
    episodes.drop
    feeds.drop
  }

  /* TODO
   private def registerDriverShutdownHook(mongoDriver: MongoDriver): Unit =
      lifecycle.addStopHook { () =>
        logger.info(s"[$lnm] Stopping the MongoDriver...")
        Future(mongoDriver.close())
      }
   */

  /*
  private val podcastMapper = OldPodcastMapper.INSTANCE
  private val episodeMapper = OldEpisodeMapper.INSTANCE
  private val feedMapper = OldFeedMapper.INSTANCE
  private val chapterMapper = OldChapterMapper.INSTANCE
  private val indexMapper = OldIndexMapper.INSTANCE
  private val idMapper = OldIdMapper.INSTANCE
  */

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

    case CheckPodcast(id) => onCheckPodcast(id)

    case CheckFeed(id) => onCheckFeed(id)

    //case CheckAllPodcasts => onCheckAllPodcasts(0, config.maxPageSize)

    case CheckAllFeeds => onCheckAllFeeds(0, config.maxPageSize)

    case FeedStatusUpdate(podcastId, feedUrl, timestamp, status) => onFeedStatusUpdate(podcastId, feedUrl, timestamp, status)

    //case SaveChapter(chapter) => onSaveChapter(chapter)

    //case AddPodcastAndFeedIfUnknown(podcast, feed) => onAddPodcastAndFeedIfUnknown(podcast, feed)

    case UpdatePodcast(id, url, podcast) => onUpdatePodcast(id, url, podcast)

    case UpdateEpisode(episode) => onUpdateEpisode(episode)

    // TODO
    //case UpdateFeed(podcastExo, feed) =>  ...
    //case UpdateChapter(episodeExo, chapter) =>  ...

    case UpdateFeedUrl(oldUrl, newUrl) => onUpdateFeedMetadataUrl(oldUrl, newUrl)

    case UpdateLinkById(id, newUrl) => onUpdateLinkById(id, newUrl)

    case GetPodcast(id) => onGetPodcast(id)

    case GetAllPodcasts(page, size) => onGetAllPodcasts(page, size)

    case GetAllPodcastsRegistrationComplete(page, size) => onGetAllPodcastsRegistrationComplete(page, size)

    case GetAllFeeds(page, size) => onGetAllFeeds(page, size)

    case GetEpisode(id) => onGetEpisode(id)

    case GetEpisodesByPodcast(podcastId) => onGetEpisodesByPodcast(podcastId)

    case GetFeedsByPodcast(podcastId) => onGetFeedsByPodcast(podcastId)

    case GetChaptersByEpisode(episodeId) => onGetChaptersByEpisode(episodeId)

    case GetFeed(id) => onGetFeed(id)

    case RegisterEpisodeIfNew(podcastId, episode) => onRegisterEpisodeIfNew(podcastId, episode)

    case DebugPrintAllPodcasts => debugPrintAllPodcasts()

    case DebugPrintAllEpisodes => debugPrintAllEpisodes()

    case DebugPrintAllFeeds => debugPrintAllFeeds()

    case unhandled => log.warning("Received unhandled message of type : {}", unhandled.getClass)

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
            val now = LocalDateTime.now()

            val podcastId = idGenerator.newId
            val podcast = Podcast(
              id          = Some(podcastId),
              title       = Some(podcastId),
              description = Some(url),
              registration = PodcastRegistrationInfo(
                complete  = Some(false),
                timestamp = Some(now)
              )
            )
            /*
            var podcast = ImmutableOldPodcast.builder()
              .setId(podcastId)
              //.setExo(podcastId)
              .setTitle(podcastId)
              .setDescription(url)
              .setRegistrationComplete(false)
              .setRegistrationTimestamp(LocalDateTime.now())
              .create()
              */
            val feedId = idGenerator.newId
            val feed = Feed(
              id                    = Some(feedId),
              podcastId             = Some(podcastId),
              url                   = Some(url),
              lastChecked           = Some(now),
              lastStatus            = Some(FeedStatus.NEVER_CHECKED),
              registrationTimestamp = Some(now),
            )
            /*
            val feed = ImmutableOldFeed.builder()
              .setId(feedId)
              .setPodcastId(podcastId)
              //.setExo(feedId)
              //.setPodcastExo(podcastId)
              .setUrl(url)
              .setLastChecked(LocalDateTime.now())
              .setLastStatus(FeedStatus.NEVER_CHECKED)
              .setRegistrationTimestamp(LocalDateTime.now())
              .create()
              */

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
                    updater ! ProcessFeed(podcastId, url, NewPodcastFetchJob())
                  })
              })
          } else {
            log.info("Feed URL is already in database : {}", url)
          }
        case Failure(ex) => onError("Could not get all feeds by URL="+url, ex)
      }

  }

  private def onFeedStatusUpdate(podcastId: String, url: String, timestamp: LocalDateTime, status: FeedStatus): Unit = {
    log.debug("Received FeedStatusUpdate({},{},{})", url, timestamp, status)

    feeds
      .findOneByUrlAndPodcastId(url, podcastId)
      .foreach {
        case Some(f) =>
          feeds.save(f.copy(lastChecked = Option(timestamp), lastStatus = Option(status)))
          /*
          feeds.save(feedMapper
            .toImmutable(f)
            .withLastChecked(timestamp)
            .withLastStatus(status))
            */
        case None => log.warning("No Feed found for Podcast (ID:{}) and URL : {}", podcastId, url)
      }
  }

  @Deprecated
  private def onUpdatePodcast(podcastId: String, feedUrl: String, podcast: Podcast): Unit = {
    log.debug("Received UpdatePodcast({},{},{})", podcastId, feedUrl, podcast.id)

    /* TODO
     * hier empfange ich die feedUrl die mir der Parser zurückgib, um anschließend die episode laden zu können
     * das würde ich mir gerne ersparen. dazu müsste ich aus der DB den "primärfeed" irgednwie bekommen können, also
     * jenen feed den ich immer benutze um updates zu laden
     */

    podcasts
      .findOne(podcastId)
      .map {
        case Some(p) => podcast.update(p)  //podcastMapper.update(podcast, p)
        case None =>
          log.debug("Podcast to update is not yet in database, therefore it will be added : {}", podcast.id)
          //podcastMapper.toModifiable(podcast)
          podcast
      }
      .foreach(p => {
        //p.setRegistrationComplete(true)
        //podcastService.save(p)
        podcasts.save(p.copy(registration = PodcastRegistrationInfo(complete = Some(true))))

        // TODO we will fetch feeds for checking new episodes, but not because we updated podcast metadata
        // crawler ! FetchFeedForUpdateEpisodes(feedUrl, podcastId)
      })
  }

  private def onUpdateEpisode(episode: Episode): Unit = {
    log.debug("Received UpdateEpisode({})", episode.id)

    episodes
      .findOne(episode.id)
      .map {
        case Some(e) => episode.update(e) // episodeMapper.update(episode, e)
        case None =>
          log.debug("Episode to update is not yet in database, therefore it will be added : {}", episode.id)
          //episodeMapper.toModifiable(episode)
        episode
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
              feeds.save(f.copy(url = Option(newUrl)))
              /*
              feeds.save(feedMapper
                .toImmutable(f)
                .withUrl(newUrl))
                */
            })
          } else {
            log.error("No Feed found in database with url='{}'", oldUrl)
          }
        case Failure(ex) => onError("Could not get all feeds by URL="+oldUrl, ex)
      }
  }

  private def onUpdateLinkById(id: String, newUrl: String): Unit = {
    log.debug("Received UpdateLinkById({},'{}')", id, newUrl)

    podcasts
      .findOne(id)
      .foreach {
        case Some(p) =>
          podcasts.save(p.copy(link = Option(newUrl)))
          //podcasts.save(podcastMapper.toImmutable(p).withLink(newUrl))
        case None =>
          episodes
            .findOne(id)
            .foreach {
              case Some(e) =>
                episodes.save(e.copy(link = Option(newUrl)))
                //episodes.save(episodeMapper.toImmutable(e).withLink(newUrl))
              case None    => log.error("Cannot update Link URL - no Podcast or Episode found by ID : {}", id)
            }
      }
  }

  private def onGetPodcast(id: String): Unit = {
    log.debug("Received GetPodcast('{}')", id)

    val theSender = sender()
    podcasts
      .findOne(id)
      .andThen {
        case Success(p)  => p
        case Failure(ex) =>
          onError(s"Error on retrieving Podcast (ID=$id) from database : ", ex)
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

  private def onGetEpisode(id: String): Unit= {
    log.debug("Received GetEpisode('{}')", id)

    val theSender = sender()
    episodes
      .findOne(id)
      .andThen {
        case Success(e)  => e
        case Failure(ex) =>
          onError(s"Error on retrieving Episode (ID=$id) from database : ", ex)
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
        case Some(e) => e.chapters
          /*
          Option(e.getChapters) match {
            case Some(cs) => cs.asScala.toList
            case None     => List()
          }
          */
        case None =>
          log.warning("Database does not contain Episode (ID) : {}", episodeId)
          List()
      }
      .foreach { cs => theSender ! ChaptersByEpisodeResult(cs) }
  }

  private def onGetFeed(id: String): Unit = {
    log.debug("Received GetFeed('{}')", id)

    val theSender = sender()
    feeds
      .findOne(id)
      .andThen {
        case Success(f)  => f
        case Failure(ex) =>
          onError(s"Error on retrieving Feed (ID=$id) from database : ", ex)
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
            (f.podcastId, f.url) match {
              case (Some(id), Some(url)) => updater ! ProcessFeed(id, url, UpdateEpisodesFetchJob(null, null))
              case (Some(_), None)       => log.error("Cannot check Feed -- Feed.url is None")
              case (None, Some(_))       => log.error("Cannot check Feed -- Feed.podcastId is None")
              case (None, None)          => log.error("Cannot check Feed -- Feed.podcastId and Feed.url are both None")
            }
          } else {
            log.error(s"Chould not update Podcast (ID = $podcastId) -- No feed found")
          }
        case Failure(ex) => log.error(s"Could not retrieve Feeds by Podcast (ID = $podcastId) : {}", ex)
      }
  }

  private def onCheckFeed(feedId: String): Unit = {
    log.debug("Received CheckFeed({})", feedId)

    feeds
      .findOne(feedId)
      .foreach {
        case Some(f) =>
          (f.podcastId, f.url) match {
            case (Some(id), Some(url)) => updater ! ProcessFeed(id, url, UpdateEpisodesFetchJob(null, null))
            case (Some(_), None)       => log.error("Cannot check Feed -- Feed.url is None")
            case (None, Some(_))       => log.error("Cannot check Feed -- Feed.podcastId is None")
            case (None, None)          => log.error("Cannot check Feed -- Feed.podcastId and Feed.url are both None")
          }
        case None    => log.error("No Feed in Database (ID) : {}", feedId)
      }
  }

  private def onCheckAllFeeds(page: Int, size: Int): Unit = {
    log.debug("Received CheckAllFeeds({},{})", page, size)

    // TODO
    // we need a new way to update feeds. Running an update on all Feeds isn't practical

    throw new UnsupportedOperationException("Currently not implemented")
  }

  private def onRegisterEpisodeIfNew(podcastId: String, episode: Episode): Unit = {
    log.debug("Received RegisterEpisodeIfNew({}, '{}')", podcastId, episode.title)

    podcasts
      .findOne(podcastId)
      .foreach {
        case Some(p) =>
          episode.guid
            .map(guid => {
              episodes
                .findAllByPodcastAndGuid(podcastId, guid)
                .map(es => es.headOption)
            })
            .getOrElse({
              episodes
                .findOneByEnclosure(episode.enclosure.url, episode.enclosure.length, episode.enclosure.typ)
            })
            .map {
              case Some(e) => log.debug("Episode is already registered : ('{}', {}, '{}')", episode.enclosure.url, episode.enclosure.length, episode.enclosure.typ)
              case None =>

                // generate a new episode exo - the generator is (almost) ensuring uniqueness
                val episodeId = idGenerator.newId

                val patch = Episode(
                  id           = Some(episodeId),
                  podcastId    = Option(podcastId),
                  podcastTitle = p.title,
                  image        = reduce(episode.image, p.image),
                  registration = EpisodeRegistrationInfo(
                    timestamp = Some(LocalDateTime.now())
                  )
                )

                val e = episode.update(patch)

                /*
                val e = episodeMapper.toModifiable(episode)

                e.setId(episodeId)
                e.setPodcastId(podcastId)
                //e.setExo(episodeId)
                //e.setPodcastExo(podcastId)
                e.setPodcastTitle(p.getTitle) // we'll not re-use this DTO, but extract the info again a bit further down
                e.setRegistrationTimestamp(LocalDateTime.now())

                // check if the episode has a cover image defined, and set the one of the episode
                Option(e.getImage).getOrElse({
                  e.setImage(p.getImage)
                })
                */

                // save asynchronously
                episodes
                  .save(e)
                  .onComplete {
                    case Success(_) =>
                      log.info("episode registered : '{}' [p:{},e:{}]", e.title.get, podcastId, e.id.get)

                      val indexEvent = AddDocIndexEvent(IndexMapper.toIndexDoc(e))// AddDocIndexEvent(indexMapper.toImmutable(e))
                      //emitIndexEvent(indexEvent)
                      indexStore ! indexEvent

                      /* TODO send an update to all catalogs via the broker, so all other stores will have
                       * the data too (this will of course mean that I will update my own data, which is a
                       * bit pointless, by oh well... */
                      //val catalogEvent = UpdateEpisode(podcastExo, idMapper.clearImmutable(e))
                      //emitCatalogEvent(catalogEvent)
                      //self ! catalogEvent

                      // request that the website will get added to the episodes index entry as well
                      /*
                      Option(e.getLink) match {
                        case Some(url) => updater ! ProcessFeed(e.getId, url, WebsiteFetchJob())
                        case None      => log.debug("No link set for episode {} --> no website data will be added to the index", episode.getId)
                      }
                      */
                      e.link match {
                        case Some(url) =>
                          e.id match {
                            case Some(id) => updater ! ProcessFeed(id, url, WebsiteFetchJob())
                            case None     => log.error(s"Cannot send ProcessFeed (_,$url,WebsiteFetchJob) message -- Episode.id is None")
                          }
                        case None      => log.debug("No link set for episode {} --> no website data will be added to the index", episode.id)
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
        case None => log.error("Could not register Episode -- No  parent Podcast for (ID) : {}", podcastId)
      }
  }

  private def debugPrintAllPodcasts(): Unit = {
    log.debug("Received DebugPrintAllPodcasts")
    log.info("All Podcasts in database:")

    podcasts
      .findAll(0, config.maxPageSize)
      .onComplete {
        case Success(ps) => ps.foreach(p => println(s"${p.id} : ${p.title}"))
        case Failure(ex) => log.error("Could not retrieve and print all Podcasts : {}", ex)
      }
  }

  private def debugPrintAllEpisodes(): Unit = {
    log.debug("Received DebugPrintAllEpisodes")
    log.info("All Episodes in database:")

    episodes
      .findAll(0, config.maxPageSize)
      .onComplete {
        case Success(es) => es.foreach(e => println(s"${e.id} : ${e.title}"))
        case Failure(ex) => log.error("Could not retrieve and print all Episodes : {}", ex)
      }
  }

  private def debugPrintAllFeeds(): Unit = {
    log.debug("Received DebugPrintAllFeeds")
    log.info("All Feeds in database:")

    feeds
      .findAll(0, config.maxPageSize)
      .onComplete {
        case Success(fs) => fs.foreach(f => println(s"${f.id} : ${f.url}"))
        case Failure(ex) => log.error("Could not retrieve and print all Feeds : {}", ex)
      }
  }

}
