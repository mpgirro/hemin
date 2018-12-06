package io.hemin.engine.catalog

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.hemin.engine.catalog.CatalogStore._
import io.hemin.engine.catalog.repository.{EpisodeRepository, FeedRepository, ImageRepository, PodcastRepository}
import io.hemin.engine.crawler.Crawler._
import io.hemin.engine.index.IndexStore.AddDocIndexEvent
import io.hemin.engine.model._
import io.hemin.engine.model.info.PodcastRegistration
import io.hemin.engine.node.Node._
import io.hemin.engine.updater.Updater.ProcessFeed
import io.hemin.engine.util.IdGenerator
import io.hemin.engine.util.mapper.IndexMapper
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object CatalogStore {
  final val name = "catalog"
  def props(config: CatalogConfig): Props =
    Props(new CatalogStore(config))
      .withDispatcher(config.dispatcher)
      .withMailbox(config.mailbox)

  trait CatalogMessage
  trait CatalogEvent extends CatalogMessage
  trait CatalogCommand extends CatalogMessage
  trait CatalogQuery extends CatalogMessage
  trait CatalogQueryResult extends CatalogMessage
  // CatalogCommands
  final case class ProposeNewFeed(url: String) extends CatalogCommand                 // Web/CLI -> CatalogStore
  final case class RegisterEpisodeIfNew(podcastId: String, episode: Episode) extends CatalogCommand // Questions: Parser -> CatalogStore
  // CatalogEvents
  //case class AddPodcastAndFeedIfUnknown(podcast: OldPodcast, feed: OldFeed) extends CatalogEvent
  final case class FeedStatusUpdate(podcastId: String, feedUrl: String, timestamp: LocalDateTime, status: FeedStatus) extends CatalogEvent
  final case class UpdateFeedUrl(oldUrl: String, newUrl: String) extends CatalogEvent
  final case class UpdateLinkById(id: String, newUrl: String) extends CatalogEvent
  final case class SaveChapter(chapter: Chapter) extends CatalogEvent
  final case class UpdatePodcast(podcastId: String, feedUrl: String, podcast: Podcast) extends CatalogEvent
  final case class UpdateEpisode(episode: Episode) extends CatalogEvent
  final case class UpdateEpisodeWithChapters(podcastId: String, episode: Episode, chapter: List[Chapter]) extends CatalogEvent
  final case class UpdateImage(image: Image) extends CatalogEvent
  // CatalogQueries
  final case class GetPodcast(id: String) extends CatalogQuery
  final case class GetAllPodcasts(page: Option[Int], size: Option[Int]) extends CatalogQuery
  final case class GetAllPodcastsRegistrationComplete(page: Option[Int], size: Option[Int]) extends CatalogQuery
  final case class GetAllFeeds(page: Option[Int], size: Option[Int]) extends CatalogQuery
  final case class GetEpisode(id: String) extends CatalogQuery
  final case class GetEpisodesByPodcast(podcastId: String) extends CatalogQuery
  final case class GetFeedsByPodcast(podcastId: String) extends CatalogQuery
  final case class GetChaptersByEpisode(episodeId: String) extends CatalogQuery
  final case class GetFeed(id: String) extends CatalogQuery
  final case class GetImage(id: String) extends CatalogQuery
  final case class GetImageByUrl(url: String) extends CatalogQuery
  //final case class GetImageByPodcast(id: String) extends CatalogQuery
  //final case class GetImageByEpisode(id: String) extends CatalogQuery
  final case class CheckPodcast(id: String) extends CatalogQuery
  final case class CheckFeed(id: String) extends CatalogQuery
  //case class CheckAllPodcasts() extends CatalogQuery
  final case class CheckAllFeeds() extends CatalogQuery
  // CatalogQueryResults
  final case class PodcastResult(podcast: Option[Podcast]) extends CatalogQueryResult
  final case class AllPodcastsResult(podcasts: List[Podcast]) extends CatalogQueryResult
  final case class AllFeedsResult(results: List[Feed]) extends CatalogQueryResult
  final case class EpisodeResult(episode: Option[Episode]) extends CatalogQueryResult                      // TODO make it an option, and remove NothingFound message
  final case class EpisodesByPodcastResult(episodes: List[Episode]) extends CatalogQueryResult
  final case class FeedsByPodcastResult(feeds: List[Feed]) extends CatalogQueryResult
  final case class ChaptersByEpisodeResult(chapters: List[Chapter]) extends CatalogQueryResult
  final case class FeedResult(feed: Option[Feed]) extends CatalogQueryResult
  final case class ImageResult(image: Option[Image]) extends CatalogQueryResult
  //case class NothingFound(exo: String) extends CatalogQueryResult
}

class CatalogStore(config: CatalogConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher : {}", self.path.name, context.system.dispatchers.lookup(context.props.dispatcher))
  log.debug("{} running with mailbox : {}", self.path.name, context.system.mailboxes.lookup(context.props.mailbox))

  private implicit val executionContext: ExecutionContext = context.dispatcher

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

  @inline private def resolveDB: Future[DefaultDB] =
    connection.database(dbName).andThen {
      case _ => /*logger.debug*/ log.debug(s"[$lnm] MongoDB resolved: $dbName")
    }

  //private def db(implicit ec: ExecutionContext): DefaultDB = Await.result(resolveDB(ec), 10.seconds)

  private val podcasts: PodcastRepository = new PodcastRepository(resolveDB, executionContext)
  private val episodes: EpisodeRepository = new EpisodeRepository(resolveDB, executionContext)
  private val feeds: FeedRepository = new FeedRepository(resolveDB, executionContext)
  private val images: ImageRepository = new ImageRepository(resolveDB, executionContext)
  //private val chapters: ChapterRepository = new ChapterRepository(db, executionContext)

  // white all data if we please
  if (config.createDatabase) {
    log.info("Dropping database collections on startup")
    podcasts.drop
    episodes.drop
    feeds.drop
    images.drop
  }

  /* TODO
   private def registerDriverShutdownHook(mongoDriver: MongoDriver): Unit =
      lifecycle.addStopHook { () =>
        logger.info(s"[$lnm] Stopping the MongoDriver...")
        Future(mongoDriver.close())
      }
   */

  override def postRestart(cause: Throwable): Unit = {
    log.warning("{} has been restarted or resumed", self.path.name)

    //log.info(s"[$lnm] Stopping the MongoDriver...")
    //Future(mongoDriver.close())

    super.postRestart(cause)
  }

  override def postStop(): Unit = {
    log.info("{} subsystem shutting down", CatalogStore.name.toUpperCase)
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

    case UpdateImage(image) => onUpdateImage(image)

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

    case GetImage(id) => onGetImage(id)

    /*
    case GetImageByPodcast(id) => onGetImageByPodcast(id)

    case GetImageByEpisode(id) => onGetImageByEpisode(id)
    */

    case GetImageByUrl(url) => onGetImageByUrl(url)

    case RegisterEpisodeIfNew(podcastId, episode) => onRegisterEpisodeIfNew(podcastId, episode)

    case DebugPrintAllPodcasts => debugPrintAllPodcasts()

    case DebugPrintAllEpisodes => debugPrintAllEpisodes()

    case DebugPrintAllFeeds => debugPrintAllFeeds()

  }

  override def unhandled(msg: Any): Unit = {
    super.unhandled(msg)
    log.error("Received unhandled message of type : {}", msg.getClass)
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
              //title       = Some(podcastId),
              //description = Some(url),
              registration = PodcastRegistration(
                complete  = Some(false),
                timestamp = Some(now)
              )
            )

            val feedId = idGenerator.newId
            val feed = Feed(
              id                    = Some(feedId),
              podcastId             = Some(podcastId),
              url                   = Some(url),
              lastChecked           = Some(now),
              lastStatus            = Some(FeedStatus.NeverChecked),
              registrationTimestamp = Some(now),
            )

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
        case Some(f) => feeds.save(f.copy(lastChecked = Option(timestamp), lastStatus = Option(status)))
        case None    => log.warning("No Feed found for Podcast (ID:{}) and URL : {}", podcastId, url)
      }
  }

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
        case Some(p) =>

          // TODO once we support cluster mode, we only want to dispatch this message once, not on all nodes
          if (config.storeImages && !p.image.equals(podcast.image)) {
            podcast.image.foreach { img =>
              crawler ! DownloadWithHeadCheck(podcastId, img, ImageFetchJob())
            }
          }

          podcast.patchRight(p)
        case None =>
          log.debug("Podcast to update is not yet in database, therefore it will be added : {}", podcast.id)
          podcast
      }
      .map(_.copy(registration = PodcastRegistration(complete = Some(true))))
      .foreach(p => {
        podcasts.save(p)

        // TODO we will fetch feeds for checking new episodes, but not because we updated podcast metadata
        // crawler ! FetchFeedForUpdateEpisodes(feedUrl, podcastId)
      })
  }

  private def onUpdateEpisode(episode: Episode): Unit = {
    log.debug("Received UpdateEpisode({})", episode.id)

    episodes
      .findOne(episode.id)
      .map {
        case Some(e: Episode) =>

          // TODO once we support cluster mode, we only want to dispatch this message once, not on all nodes
          if (config.storeImages && !e.image.equals(episode.image)) {
            episode.image.foreach { img =>
              crawler ! DownloadWithHeadCheck(e.podcastId.get, img, ImageFetchJob())
            }
          }

          episode.patchRight(e) // episodeMapper.update(episode, e)
        case None =>
          log.debug("Episode to update is not yet in database, therefore it will be added : {}", episode.id)
          episode
      }
      .foreach(e => {
        episodes.save(e)
      })
  }

  private def updateImageReferenceOfPodcasts(url: Option[String], id: Option[String]): Unit = {
    log.debug("Updating the Image reference of Podcasts from URL = '{}' to ID = {}", url, id)

    (url, id) match {
      case (None, None) => log.warning("Cannot update image reference in Podcast; reason: provided URL and ID are None")
      case (None, _)    => log.warning("Cannot update image reference in Podcast; reason: provided URL is None")
      case (_, None)    => log.warning("Cannot update image reference in Podcast; reason: provided ID is None")
      case (Some(u), Some(i)) =>
        podcasts
          .findAllByImage(u)
          .andThen {
            case Success(ps) => ps
            case Failure(ex) =>
              onError(s"Could not get all Podcasts by image=$u", ex)
              Nil // we have no results to return
          }
          .map { _
            .foreach { p =>
              podcasts.save(p.copy(image = id))
            }
          }
    }
  }

  private def updateImageReferenceOfEpisodes(url: Option[String], id: Option[String]): Unit = {
    log.debug("Updating the Image reference of Episodes from URL = '{}' to ID = {}", url, id)

    (url, id) match {
      case (None, None) => log.warning("Cannot update image reference in Episode; reason: provided URL and ID are None")
      case (None, _)    => log.warning("Cannot update image reference in Episode; reason: provided URL is None")
      case (_, None)    => log.warning("Cannot update image reference in Episode; reason: provided ID is None")
      case (Some(u), Some(i)) =>
        episodes
          .findAllByImage(u)
          .andThen {
            case Success(es) => es
            case Failure(ex) =>
              onError(s"Could not get all Episodes by image=$u", ex)
              Nil // we have no results to return
          }
          .map { _
            .foreach { e =>
              episodes.save(e.copy(image = id))
            }
          }
    }
  }

  private def onUpdateImage(image: Image): Unit = {
    log.debug("Received UpdateImage(url = '{}')", image.url)

    image.url match {
      case Some(url) =>
        images
          .findOneByUrl(url)
          .map {
            case Some(i) => // update existing
              image.patchRight(i)
            case None => // add as new
              log.debug("Image to update is not yet in database, therefore it will be added : {} (URL)", url)
              val id = idGenerator.newId
              image.copy(id = Some(id))
          }
          .foreach(i => {
            images.save(i)

            updateImageReferenceOfPodcasts(i.url, i.id)
            updateImageReferenceOfEpisodes(i.url, i.id)
          })
      case None => log.error("Could not add-or-update image; reason: associateId was None (this should not be possible and is a programmer error)")
    }
  }

  private def onUpdateFeedMetadataUrl(oldUrl: String, newUrl: String): Unit = {
    log.debug("Received UpdateFeedUrl('{}','{}')", oldUrl, newUrl)

    feeds
      .findAllByUrl(oldUrl)
      .onComplete {
        case Success(fs) =>
          if (fs.nonEmpty) {
            fs.foreach(f => feeds.save(f.copy(url = Option(newUrl))))
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
        case Some(p) => podcasts.save(p.copy(link = Option(newUrl)))
        case None =>
          episodes
            .findOne(id)
            .foreach {
              case Some(e) => episodes.save(e.copy(link = Option(newUrl)))
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

  private def onGetAllPodcasts(page: Option[Int], size: Option[Int]): Unit = {
    log.debug("Received GetAllPodcasts({},{})", page, size)

    val p: Int = page.getOrElse(config.defaultPage)
    val s: Int = size.getOrElse(config.defaultSize)

    val theSender = sender()
    podcasts
      .findAll(p, s)
      .andThen {
        case Success(ps) => ps
        case Failure(ex) =>
          onError(s"Could not get all Podcasts by page=$page and size=$size", ex)
          Nil // we have no results to return
      }
      .map { ps =>
        theSender ! AllPodcastsResult(ps)
      }
  }

  private def onGetAllPodcastsRegistrationComplete(page: Option[Int], size: Option[Int]): Unit = {
    log.debug("Received GetAllPodcastsRegistrationComplete({},{})", page, size)

    val p: Int = page.getOrElse(config.defaultPage)
    val s: Int = size.getOrElse(config.defaultSize)

    val theSender = sender()
    podcasts
      .findAllRegistrationCompleteAsTeaser(p, s)
      .andThen {
        case Success(ps) => ps
        case Failure(ex) =>
          onError(s"Could not get all Podcasts by page=$page and size=$size and registrationCompelete=TRUE", ex)
          Nil // we have no results to return
      }
      .map { ps =>
        theSender ! AllPodcastsResult(ps)
      }
  }

  private def onGetAllFeeds(page: Option[Int], size: Option[Int]): Unit = {
    log.debug("Received GetAllFeeds({},{})", page, size)

    val p: Int = page.getOrElse(config.defaultPage)
    val s: Int = size.getOrElse(config.defaultSize)

    val theSender = sender()
    feeds
      .findAll(p, s)
      .andThen {
        case Success(fs) => fs
        case Failure(ex) =>
          onError(s"Could not get all Feeds by page=$page and size=$size", ex)
          Nil // we have no results to return
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
          Nil // we have no results to return
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
          Nil // we have no results to return
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
        case None =>
          log.warning("Database does not contain Episode (ID) : {}", episodeId)
          Nil
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

  private def onGetImage(id: String): Unit = {
    log.debug("Received GetImage('{}')", id)
    val theSender = sender()
    images
      .findOne(id)
      .andThen {
        case Success(i)  => i
        case Failure(ex) =>
          onError(s"Error on retrieving Image (ID=$id) from database : ", ex)
          None // we have no results to return
      }
      .foreach(i => theSender ! ImageResult(i))
  }

  private def onGetImageByUrl(url: String): Unit = {
    log.debug("Received GetImageByUrl('{}')", url)
    val theSender = sender()
    images
      .findOneByUrl(url)
      .andThen {
        case Success(i)  => i
        case Failure(ex) =>
          onError(s"Error on retrieving Image by URL = '$url' from database : ", ex)
          None // we have no results to return
      }
      .foreach(i => theSender ! ImageResult(i))
  }

  /*
  private def onGetImageByPodcast(id: String): Unit = {
    log.debug("Received GetImageByPodcast('{}')", id)
    val theSender = sender()
    podcasts
      .findOne(id)
      .map {
        case Some(p) => images.findOne(p.image)
        case None    =>
          log.warning("No image found for podcast : {}", id)
          None // no podcast not image
      }
      .mapTo[Option[Image]]
      .foreach(i => theSender ! ImageResult(i))
  }

  private def onGetImageByEpisode(id: String): Unit = {
    log.debug("Received GetImageByEpisode('{}')", id)
    val theSender = sender()
    episodes
      .findOne(id)
      .map {
        case Some(e) => images.findOne(e.image)
        case None    =>
          log.warning("No image found for episode : {}", id)
          None // no episode not image
      }
      .mapTo[Option[Image]]
      .foreach(i => theSender ! ImageResult(i))
  }
  */

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
              case Some(e: Episode) =>
                log.debug("Episode is already registered : ('{}', {}, '{}')", episode.enclosure.url, episode.enclosure.length, episode.enclosure.typ)

                val catalogEvent = CatalogStore.UpdateEpisode(episode)
                //emitCatalogEvent(catalogEvent)
                self ! catalogEvent

              case None =>

                // generate a new episode exo - the generator is (almost) ensuring uniqueness
                val episodeId = idGenerator.newId

                // TODO once we support cluster mode, we only want to dispatch this message once, not on all nodes
                if (config.storeImages) {
                  episode.image.foreach { img =>
                    crawler ! DownloadWithHeadCheck(podcastId, img, ImageFetchJob())
                  }
                }

                val e = episode
                  .copy(
                    id           = Some(episodeId),
                    podcastId    = Some(podcastId),
                    podcastTitle = p.title,
                    registration = EpisodeRegistration(
                      timestamp = Some(LocalDateTime.now())
                    ),
                    chapters = episode.chapters.map(_.copy(episodeId = Some(episodeId))), // TODO are chapters now embedded, remove episodeId
                  )
                  .patchLeft(Episode(
                    image = p.image
                  ))

                // save asynchronously
                episodes
                  .save(e)
                  .onComplete {
                    case Success(_) =>
                      log.info("episode registered : '{}' [p:{},e:{}]", e.title.get, podcastId, e.id.get)

                      IndexMapper.toIndexDoc(e) match {
                        case Success(doc) =>
                          val indexEvent = AddDocIndexEvent(doc)
                          //emitIndexEvent(indexEvent)
                          indexStore ! indexEvent
                        case Failure(ex) =>
                          log.error("Failed to map Episode to IndexDoc; reason : {}", ex.getMessage)
                          ex.printStackTrace()
                      }

                      /* TODO send an update to all catalogs via the broker, so all other stores will have
                       * the data too (this will of course mean that I will update my own data, which is a
                       * bit pointless, by oh well... */
                      //val catalogEvent = UpdateEpisode(podcastExo, idMapper.clearImmutable(e))
                      //emitCatalogEvent(catalogEvent)
                      //self ! catalogEvent

                      // request that the website will get added to the episodes index entry as well
                      (e.id, e.link) match {
                        case (Some(id), Some(url)) => updater ! ProcessFeed(id, url, WebsiteFetchJob())
                        case (_, None)             => log.debug("No link set for episode {} --> no website data will be added to the index", e.id)
                        case (None, _)             => log.error(s"Cannot send ProcessFeed (_,${e.link},WebsiteFetchJob) message -- Episode.id is None")
                      }

                    case Failure(ex) => onError("Could not save new Episode", ex)
                  }

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
