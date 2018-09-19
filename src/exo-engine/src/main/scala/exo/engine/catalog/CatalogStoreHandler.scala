package exo.engine.catalog

import java.time.LocalDateTime

import javax.persistence.{EntityManager, EntityManagerFactory}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.typesafe.config.ConfigFactory
import exo.engine.EngineProtocol._
import exo.engine.catalog.CatalogStore._
import exo.engine.catalog.mongo.ChapterMongoRepository
import exo.engine.catalog.repository.RepositoryFactoryBuilder
import exo.engine.catalog.service._
import exo.engine.config.CatalogConfig
import exo.engine.crawler.Crawler.{NewPodcastFetchJob, UpdateEpisodesFetchJob, WebsiteFetchJob}
import exo.engine.domain.FeedStatus
import exo.engine.domain.dto._
import exo.engine.index.IndexStore.{AddDocIndexEvent, IndexEvent}
import exo.engine.mapper._
import exo.engine.updater.Updater.ProcessFeed
import exo.engine.util.ExoGenerator
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.transaction.support.TransactionSynchronizationManager
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, blocking}

/**
  * @author Maximilian Irro
  */

object CatalogStoreHandler {
    def name(workerIndex: Int): String = "handler-" + workerIndex
    def props(workerIndex: Int, config: CatalogConfig): Props = {
        Props(new CatalogStoreHandler(workerIndex, config)).withDispatcher("echo.catalog.dispatcher")
    }
}

class CatalogStoreHandler(workerIndex: Int,
                          config: CatalogConfig) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    /*
    private val CONFIG = ConfigFactory.load()
    private val MAX_PAGE_SIZE: Int = Option(CONFIG.getInt("echo.catalog.max-page-size")).getOrElse(10000)
    */

    /*
    private val catalogEventStream = CONFIG.getString("echo.catalog.event-stream")
    private val indexEventStream = CONFIG.getString("echo.index.event-stream")
    private val mediator = DistributedPubSub(context.system).mediator
    */

    private val exoGenerator: ExoGenerator = new ExoGenerator(workerIndex)

    private var catalogStore: ActorRef = _
    private var indexStore: ActorRef = _
    private var crawler: ActorRef = _
    private var updater: ActorRef = _
    private var supervisor: ActorRef = _

    private var repositoryFactoryBuilder = new RepositoryFactoryBuilder(config.databaseUrl)
    private var emf: EntityManagerFactory = repositoryFactoryBuilder.getEntityManagerFactory



    // TODO experimental
    val dbName = "exodb"

    // My settings (see available connection options)
    val mongoUri = s"mongodb://localhost:27017/$dbName" // ?authMode=scram-sha1

    // TODO pass the actors EX
    import ExecutionContext.Implicits.global // use any appropriate context


    // Connect to the database: Must be done only once per application
    val driver = MongoDriver()
    val parsedUri = MongoConnection.parseURI(mongoUri)
    //val connection = parsedUri.map(driver.connection(_))
    val connection: MongoConnection = driver.connection(List("localhost"))

    /*
    // Database and collections: Get references
    val futureConnection: Future[MongoConnection] = Future.fromTry(connection)
    //def db: Future[DefaultDB] = futureConnection.flatMap(_.database(dbName))
    val mongoConnection: MongoConnection = Await.result(futureConnection, 10.seconds)
    val db: DefaultDB = Await.result(mongoConnection.database(dbName), 10.seconds)
    */
    //val db: Future[DefaultDB] = connection.database(dbName)
    val db: DefaultDB = Await.result(connection.database(dbName), 10.seconds) // TODO read timeout from config

    //val mongoService = new ChapterMongoRepository(db)

    // - - - - - - - - -



    private val podcastService = new PodcastCatalogService(log, repositoryFactoryBuilder,db)
    private val episodeService = new EpisodeCatalogService(log, repositoryFactoryBuilder,db)
    private val feedService = new FeedCatalogService(log, repositoryFactoryBuilder, db)
    private val chapterService = new ChapterCatalogService(log, repositoryFactoryBuilder,db)

    private val podcastMapper = PodcastMapper.INSTANCE
    private val episodeMapper = EpisodeMapper.INSTANCE
    private val feedMapper = FeedMapper.INSTANCE
    private val chapterMapper = ChapterMapper.INSTANCE
    private val indexMapper = IndexMapper.INSTANCE
    private val idMapper = IdMapper.INSTANCE

    override def postRestart(cause: Throwable): Unit = {
        log.info("{} has been restarted or resumed", self.path.name)

        repositoryFactoryBuilder = new RepositoryFactoryBuilder(config.databaseUrl)
        emf = repositoryFactoryBuilder.getEntityManagerFactory

        super.postRestart(cause)
    }

    override def postStop(): Unit = {
        log.info("shutting down")

        Option(emf)
            .filter(_.isOpen)
            .foreach(_.close())
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

        case ProposeNewFeed(feedUrl) => proposeFeed(feedUrl)

        case CheckPodcast(exo) => onCheckPodcast(exo)

        case CheckFeed(exo) => onCheckFeed(exo)

        case CheckAllPodcasts => onCheckAllPodcasts(0, config.maxPageSize)

        case CheckAllFeeds => onCheckAllFeeds(0, config.maxPageSize)

        case FeedStatusUpdate(podcastExo, feedUrl, timestamp, status) => onFeedStatusUpdate(podcastExo, feedUrl, timestamp, status)

        case SaveChapter(chapter) => onSaveChapter(chapter)

        case AddPodcastAndFeedIfUnknown(podcast, feed) => onAddPodcastAndFeedIfUnknown(podcast, feed)

        case UpdatePodcast(exo, url, podcast) => onUpdatePodcast(exo, url, podcast)

        case UpdateEpisode(podcastExo, episode) => onUpdateEpisode(podcastExo, episode)

        // TODO
        //case UpdateFeed(podcastExo, feed) =>  ...
        //case UpdateChapter(episodeExo, chapter) =>  ...

        case UpdateFeedUrl(oldUrl, newUrl) => onUpdateFeedMetadataUrl(oldUrl, newUrl)

        case UpdateLinkByExo(exo, newUrl) => onUpdateLinkByExo(exo, newUrl)

        case GetPodcast(podcastExo) => onGetPodcast(podcastExo)

        case GetAllPodcasts(page, size) => onGetAllPodcasts(page, size)

        case GetAllPodcastsRegistrationComplete(page, size) => onGetAllPodcastsRegistrationComplete(page, size)

        case GetAllFeeds(page, size) => onGetAllFeeds(page, size)

        case GetEpisode(podcastExo) => onGetEpisode(podcastExo)

        case GetEpisodesByPodcast(podcastExo) => onGetEpisodesByPodcast(podcastExo)

        case GetFeedsByPodcast(podcastExo) => onGetFeedsByPodcast(podcastExo)

        case GetChaptersByEpisode(episodeExo) => onGetChaptersByEpisode(episodeExo)

        case RegisterEpisodeIfNew(podcastExo, episode) => onRegisterEpisodeIfNew(podcastExo, episode)

        case DebugPrintAllPodcasts => debugPrintAllPodcasts()

        case DebugPrintAllEpisodes => debugPrintAllEpisodes()

        case DebugPrintAllFeeds => debugPrintAllFeeds()

        case DebugPrintCountAllPodcasts => debugPrintCountAllPodcasts()

        case DebugPrintCountAllEpisodes => debugPrintCountAllEpisodes()

        case DebugPrintCountAllFeeds => debugPrintCountAllFeeds()

    }

    /*
    private def emitCatalogEvent(event: CatalogEvent): Unit = {
        mediator ! Publish(catalogEventStream, event)
    }

    private def emitIndexEvent(event: IndexEvent): Unit = {
        mediator ! Publish(indexEventStream, event)
    }
    */

    private def proposeFeed(url: String): Unit = {
        log.debug("Received msg proposing a new feed: " + url)

        def task = () => {
            if(feedService.findAllByUrl(url).isEmpty){

                // TODO for now we always create a podcast for an unknown feed, but we will have to check if the feed is an alternate to a known podcast

                val podcastExo = exoGenerator.getNewExo
                var podcast = ImmutablePodcast.builder()
                    .setExo(podcastExo)
                    .setTitle(podcastExo)
                    .setDescription(url)
                    .setRegistrationComplete(false)
                    .setRegistrationTimestamp(LocalDateTime.now())
                    .create()

                podcastService.save(podcast).map(p => {

                    val feedExo = exoGenerator.getNewExo
                    val feed = ImmutableFeed.builder()
                        .setExo(feedExo)
                        .setUrl(url)
                        .setLastChecked(LocalDateTime.now())
                        .setLastStatus(FeedStatus.NEVER_CHECKED)
                        .setPodcastId(p.getId)
                        .setRegistrationTimestamp(LocalDateTime.now())
                        .create()
                    feedService.save(feed).map(f => {

                        val catalogEvent = AddPodcastAndFeedIfUnknown(
                            idMapper.clearImmutable(p),
                            idMapper.clearImmutable(f))
                        //emitCatalogEvent(catalogEvent)
                        self ! catalogEvent

                        updater ! ProcessFeed(podcastExo, f.getUrl, NewPodcastFetchJob())
                    })
                })
            } else {
                log.info("Proposed feed is already in database: {}", url)
            }
        }
        doInTransaction(task, List(podcastService, feedService))
    }

    private def onFeedStatusUpdate(podcastExo: String, url: String, timestamp: LocalDateTime, status: FeedStatus): Unit = {
        log.debug("Received FeedStatusUpdate({},{},{})", url, timestamp, status)
        def task = () => {
            feedService.findOneByUrlAndPodcastExo(url, podcastExo).map(f => {
                val feed = feedMapper.toModifiable(f)
                feed.setLastChecked(timestamp)
                feed.setLastStatus(status)
                feedService.save(feed)
            }).getOrElse({
                log.error("Received UNKNOWN FEED/Podcast FeedStatusUpdate({},{},{})", url, timestamp, status)
            })
        }
        doInTransaction(task, List(feedService))
    }

    private def onSaveChapter(chapter: Chapter): Unit = {
        log.debug("Received SaveChapter('{}') for episode : ", chapter.getTitle, chapter.getEpisodeExo)

        def task = () => {
            episodeService.findOneByExo(chapter.getEpisodeExo).map(e => {
                val c = chapterMapper.toModifiable(chapter)
                c.setEpisodeId(e.getId)
                chapterService.save(c)
            }).getOrElse({
                log.error("Could not save Chapter, no Episode (EXO) : {}", chapter.getEpisodeExo)
            })
        }
        doInTransaction(task, List(episodeService, chapterService))
    }

    private def onAddPodcastAndFeedIfUnknown(podcast: Podcast, feed: Feed): Unit = {
        log.debug("Received AddPodcastAndFeedIfUnknown({},{})", podcast.getExo, feed.getExo)
        def task = () => {
            val podcastUpdate: ModifiablePodcast = podcastService.findOneByExo(podcast.getExo).map(p => {
                podcastMapper.toModifiable(p)
            }).getOrElse({
                log.debug("Podcast to update is not yet in database, therefore it will be added : {}", podcast.getExo)
                podcastMapper.toModifiable(podcast)
            })
            podcastService.save(podcastUpdate).map(p => {
                val feedUpdate: ModifiableFeed = feedService.findOneByExo(feed.getExo).map(f => {
                    feedMapper.toModifiable(f)
                }).getOrElse({
                    log.debug("Feed to update is not yet in database, therefore it will be added : {}", feed.getExo)
                    feedMapper.toModifiable(feed)
                })

                feedUpdate.setPodcastId(p.getId)
                feedService.save(feedUpdate)
            }).getOrElse({
                log.error("Podcast could not be safed : {}", podcastUpdate)
            })
        }
        doInTransaction(task, List(podcastService, feedService))
    }

    @Deprecated
    private def onUpdatePodcast(podcastExo: String, feedUrl: String, podcast: Podcast): Unit = {
        log.debug("Received UpdatePodcast({},{},{})", podcastExo, feedUrl, podcast.getExo)

        /* TODO
         * hier empfange ich die feedUrl die mir der Parser zurückgib, um anschließend die episode laden zu können
         * das würde ich mir gerne ersparen. dazu müsste ich aus der DB den "primärfeed" irgednwie bekommen können, also
         * jenen feed den ich immer benutze um updates zu laden
         */
        def task = () => {
            val update: ModifiablePodcast = podcastService.findOneByExo(podcastExo).map(p => {
                podcastMapper.update(podcast, p)
            }).getOrElse({
                log.debug("Podcast to update is not yet in database, therefore it will be added : {}", podcast.getExo)
                podcastMapper.toModifiable(podcast)
            })
            update.setRegistrationComplete(true)
            podcastService.save(update)

            // TODO we will fetch feeds for checking new episodes, but not because we updated podcast metadata
            // crawler ! FetchFeedForUpdateEpisodes(feedUrl, podcastId)
        }
        doInTransaction(task, List(podcastService))
    }

    private def onUpdateEpisode(podcastExo: String, episode: Episode): Unit = {
        log.debug("Received UpdateEpisode({},{})", podcastExo, episode.getExo)
        def task = () => {
            podcastService.findOneByExo(podcastExo).map(p => {
                val update: ModifiableEpisode = episodeService.findOneByExo(episode.getExo).map(e => {
                    episodeMapper.update(episode, e)
                }).getOrElse({
                    log.debug("Episode to update is not yet in database, therefore it will be added : {}", episode.getExo)
                    episodeMapper.toModifiable(episode)
                })
                update.setPodcastId(p.getId)

                // in case chapters were parsed, they were sent inside the episode, but we
                // must not save them with the episode in one pass, or they'll produce a
                // detached entity (because their episodes ID is yet unknown)
                //val chapters = update.getChapters.asScala
                //update.setChapters(null)

                val saved = episodeService.save(update).get

                // TODO we'll have to check if an episode is yet known and in the database!
                // TODO best send a message to self to handle the chapter in a separate phase
                Option(update.getChapters)
                    .foreach(_
                        .asScala
                        .map(c => chapterMapper.toModifiable(c))
                        .foreach(c => {
                            c.setEpisodeId(saved.getId)
                            c.setEpisodeExo(saved.getExo)
                            chapterService.save(c)
                        }))
            }).getOrElse({
                log.error("No Podcast found in database with EXO : {}", podcastExo)
            })
        }
        doInTransaction(task, List(podcastService, episodeService, chapterService))
    }


    private def onUpdateFeedMetadataUrl(oldUrl: String, newUrl: String): Unit = {
        log.debug("Received UpdateFeedUrl('{}','{}')", oldUrl, newUrl)
        def task = () => {
            val feeds = feedService.findAllByUrl(oldUrl)
            if (feeds.nonEmpty) {
                feeds.foreach(f => {
                    val feed = feedMapper.toModifiable(f)
                    feed.setUrl(newUrl)
                    feedService.save(feed)
                })
            } else {
                log.error("No Feed found in database with url='{}'", oldUrl)
            }
        }
        doInTransaction(task, List(feedService))
    }

    private def onUpdateLinkByExo(exo: String, newUrl: String): Unit = {
        log.debug("Received UpdateLinkByExo({},'{}')", exo, newUrl)
        def task = () => {
            podcastService.findOneByExo(exo).map(p => {
                val podcast = podcastMapper.toModifiable(p)
                podcast.setLink(newUrl)
                podcastService.save(podcast)
            }).getOrElse({
                episodeService.findOneByExo(exo).map(e => {
                    val episode = episodeMapper.toModifiable(e)
                    episode.setLink(newUrl)
                    episodeService.save(episode)
                }).getOrElse({
                    log.error("Cannot update Link URL - no Podcast or Episode found by EXO : {}", exo)
                })
            })
        }
        doInTransaction(task, List(podcastService,episodeService))
    }

    private def onGetPodcast(podcastExo: String): Unit = {
        log.debug("Received GetPodcast('{}')", podcastExo)
        def task = () => {
            podcastService.findOneByExo(podcastExo).map(p => {
                Some(p)
            }).getOrElse({
                log.error("Database does not contain Podcast (EXO) : {}", podcastExo)
                None
            })
        }
        doInTransaction(task, List(podcastService))
            .asInstanceOf[Option[Podcast]]
            .map(p => {
                sender ! PodcastResult(idMapper.clearImmutable(p))
            }).getOrElse({
                sender ! NothingFound(podcastExo)
            })
    }

    private def onGetAllPodcasts(page: Int, size: Int): Unit = {
        log.debug("Received GetAllPodcasts({},{})", page, size)
        def task = () => {
            //val podcasts = podcastService.findAllWhereFeedStatusIsNot(FeedStatus.NEVER_CHECKED) // TODO broken
            podcastService.findAll(page, size)
        }
        val podcasts = doInTransaction(task, List(podcastService)).asInstanceOf[List[Podcast]]
        sender ! AllPodcastsResult(podcasts.map(p => idMapper.clearImmutable(p)))
    }

    private def onGetAllPodcastsRegistrationComplete(page: Int, size: Int): Unit = {
        log.debug("Received GetAllPodcastsRegistrationComplete({},{})", page, size)
        def task = () => {
            podcastService.findAllRegistrationCompleteAsTeaser(page, size)
        }
        val podcasts = doInTransaction(task, List(podcastService)).asInstanceOf[List[Podcast]]
        sender ! AllPodcastsResult(podcasts.map(p => idMapper.clearImmutable(p)))
    }

    private def onGetAllFeeds(page: Int, size: Int): Unit = {
        log.debug("Received GetAllFeeds({},{})", page, size)
        def task = () => {
            feedService.findAll(page, size)
        }
        val feeds = doInTransaction(task, List(feedService)).asInstanceOf[List[Feed]]
        sender ! AllFeedsResult(feeds.map(f => idMapper.clearImmutable(f)))
    }

    private def onGetEpisode(episodeExo: String): Unit= {
        log.debug("Received GetEpisode('{}')", episodeExo)
        def task = () => {
            episodeService.findOneByExo(episodeExo).map(e => {
                Some(e)
            }).getOrElse({
                log.error("Database does not contain Episode (EXO) : {}", episodeExo)
                None
            })
        }
        doInTransaction(task, List(episodeService))
            .asInstanceOf[Option[Episode]]
            .map(e => {
                sender ! EpisodeResult(idMapper.clearImmutable(e))
            }).getOrElse({
                sender ! NothingFound(episodeExo)
            })
    }

    private def onGetEpisodesByPodcast(podcastId: String): Unit = {
        log.debug("Received GetEpisodesByPodcast('{}')", podcastId)

        def task = () => {
            episodeService.findAllByPodcastAsTeaser(podcastId)
        }
        val episodes = doInTransaction(task, List(episodeService)).asInstanceOf[List[Episode]]
        sender ! EpisodesByPodcastResult(episodes.map(e => idMapper.clearImmutable(e)))
    }

    private def onGetFeedsByPodcast(podcastId: String): Unit = {
        log.debug("Received GetFeedsByPodcast('{}')", podcastId)
        def task = () => {
            feedService.findAllByPodcast(podcastId)
        }
        val feeds = doInTransaction(task, List(feedService)).asInstanceOf[List[Feed]]
        sender ! FeedsByPodcastResult(feeds.map(f => idMapper.clearImmutable(f)))
    }

    private def onGetChaptersByEpisode(episodeId: String): Unit = {
        log.debug("Received GetChaptersByEpisode('{}')", episodeId)

        def task = () => {
            chapterService.findAllByEpisode(episodeId)
        }
        val chapters = doInTransaction(task, List(chapterService)).asInstanceOf[List[Chapter]]
        sender ! ChaptersByEpisodeResult(chapters.map(c => idMapper.clearImmutable(c)))
    }

    private def onCheckPodcast(podcastId: String): Unit = {
        log.debug("Received CheckPodcast({})", podcastId)
        def task = () => {
            // TODO hier muss ich irgendwie entscheiden, wass für einen feed ich nehme um zu updaten
            val feeds = feedService.findAllByPodcast(podcastId)
            if(feeds.nonEmpty){
                val f = feeds.head
                updater ! ProcessFeed(podcastId, f.getUrl, UpdateEpisodesFetchJob(null, null))
            } else {
                log.error("No Feeds registered for Podcast (EXO) : {}", podcastId)
            }
        }
        doInTransaction(task, List(feedService))
    }

    private def onCheckFeed(feedId: String): Unit = {
        log.debug("Received CheckFeed({})", feedId)
        def task = () => {
            // TODO hier muss ich irgendwie entscheiden, wass für einen feed ich nehme um zu updaten
            feedService.findOneByExo(feedId).map(f => {
                podcastService.findOneByFeed(feedId).map(p => {
                    updater ! ProcessFeed(p.getExo, f.getUrl, UpdateEpisodesFetchJob(null, null))
                }).getOrElse({
                    log.error("No Podcast found in Database for Feed (EXO) : {}", feedId)
                })
            }).getOrElse({
                log.error("No Feed in Database (EXO) : {}", feedId)
            })
        }
        doInTransaction(task, List(podcastService, feedService))
    }

    private def onCheckAllPodcasts(page: Int, size: Int): Unit = {
        log.debug("Received CheckAllPodcasts({}, {})", page, size)

        def task = () => {
            // TODO hier muss ich irgendwie entscheiden, wass für einen feed ich nehme um zu updaten
            podcastService.findAll(page, size).foreach(p => {
                val feeds = feedService.findAllByPodcast(p.getExo)
                if(feeds.nonEmpty){
                    val f = feeds.head
                    updater ! ProcessFeed(p.getExo, feeds.head.getUrl, UpdateEpisodesFetchJob(null, null))
                } else {
                    log.error("No Feeds registered for Podcast (EXO) : {}", p.getExo)
                }
            })
        }
        doInTransaction(task, List(podcastService, feedService))
    }

    private def onCheckAllFeeds(page: Int, size: Int): Unit = {
        log.debug("Received CheckAllFeeds({},{})", page, size)

        def task = () => {
            feedService.findAll(page, size).foreach(f => {
                podcastService.findOneByFeed(f.getExo).map{p => {
                    updater ! ProcessFeed(p.getExo, f.getUrl, NewPodcastFetchJob())
                }}.getOrElse({
                    log.error("No Podcast found in Database for Feed (EXO) : {}", f.getExo)
                })
            })
        }
        doInTransaction(task, List(podcastService, feedService))
    }

    private def onRegisterEpisodeIfNew(podcastExo: String, episode: Episode): Unit = {
        log.debug("Received RegisterEpisodeIfNew({}, '{}')", podcastExo, episode.getTitle)

        def task: () => Option[Episode] = () => {
            Option(episode.getGuid).map(guid => {
                episodeService.findAllByPodcastAndGuid(podcastExo, guid).headOption
            }).getOrElse({
                episodeService.findOneByEnclosure(episode.getEnclosureUrl, episode.getEnclosureLength, episode.getEnclosureType)
            }) match {
                case Some(e) => None
                case None =>

                    val e = episodeMapper.toModifiable(episode)

                    // generate a new episode exo - the generator is (almost) ensuring uniqueness
                    e.setExo(exoGenerator.getNewExo)

                    podcastService.findOneByExo(podcastExo).map(p => {
                        e.setPodcastId(p.getId)
                        e.setPodcastTitle(p.getTitle) // we'll not re-use this DTO, but extract the info again a bit further down

                        // check if the episode has a cover image defined, and set the one of the episode
                        Option(e.getImage).getOrElse({
                            e.setImage(p.getImage)
                        })
                    }).getOrElse({
                        log.error("No Podcast found (EXO) : {}", podcastExo)
                    })

                    e.setRegistrationTimestamp(LocalDateTime.now())
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
            }
        }

        val registeredEpisode: Option[Episode] = doInTransaction(task, List(episodeService, podcastService, chapterService)).asInstanceOf[Option[Episode]]

        // in case the episode was registered, we initiate some post processing
        registeredEpisode match {
            case Some(e) =>
                log.info("episode registered : '{}' [p:{},e:{}]", e.getTitle, podcastExo, e.getExo)

                val indexEvent = AddDocIndexEvent(indexMapper.toImmutable(e))
                //emitIndexEvent(indexEvent)
                indexStore ! indexEvent

                /* TODO send an update to all catalogs via the broker, so all other stores will have
                 * the data too (this will of course mean that I will update my own data, which is a
                 * bit pointless, by oh well... */
                val catalogEvent = UpdateEpisode(podcastExo, idMapper.clearImmutable(e))
                //emitCatalogEvent(catalogEvent)
                self ! catalogEvent

                // request that the website will get added to the episodes index entry as well
                Option(e.getLink) match {
                    case Some(link) =>
                        updater ! ProcessFeed(e.getExo, link, WebsiteFetchJob())
                    case None => log.debug("No link set for episode {} --> no website data will be added to the index", episode.getExo)
                }
            case None =>
                log.debug("Episode is already registered : ('{}', {}, '{}')",episode.getEnclosureUrl, episode.getEnclosureLength, episode.getEnclosureType)
        }
    }

    private def debugPrintAllPodcasts(): Unit = {
        log.debug("Received DebugPrintAllPodcasts")
        log.info("All Podcasts in database:")
        def task = () => {
            podcastService.findAll(0, config.maxPageSize).foreach(p => println(s"${p.getExo} : ${p.getTitle}"))
        }
        doInTransaction(task, List(podcastService))
    }

    private def debugPrintAllEpisodes(): Unit = {
        log.debug("Received DebugPrintAllEpisodes")
        log.info("All Episodes in database:")
        def task = () => {
            episodeService.findAll().foreach(e => println(s"${e.getExo} : ${e.getTitle}"))
        }
        doInTransaction(task, List(episodeService))
    }

    private def debugPrintAllFeeds(): Unit = {
        log.debug("Received DebugPrintAllFeeds")
        log.info("All Feeds in database:")
        def task = () => {
            feedService.findAll(0, config.maxPageSize).foreach(f => println(s"${f.getExo} : ${f.getUrl}"))
        }
        doInTransaction(task, List(feedService))
    }

    private def debugPrintCountAllPodcasts(): Unit = {
        log.debug("Received DebugPrintCountAllPodcasts")
        def task = () => {
            podcastService.countAll()
        }
        val count = doInTransaction(task, List(podcastService))
        log.info("Podcasts in Database : {}", count)
    }

    private def debugPrintCountAllEpisodes(): Unit = {
        log.debug("Received DebugPrintCountAllEpisodes")
        def task = () => {
            episodeService.countAll()
        }
        val count = doInTransaction(task, List(episodeService))
        log.info("Episodes in Database : {}", count)
    }

    private def debugPrintCountAllFeeds(): Unit = {
        log.debug("Received DebugPrintCountAllFeeds")
        def task = () => {
            feedService.countAll()
        }
        val count = doInTransaction(task, List(feedService))
        log.info("Feeds in Database : {}", count)
    }

    /**
      *
      * @param task the function to be executed inside a transaction
      * @param services all services used within the callable function, which therefore require a refresh before doing the work
      */
    private def doInTransaction(task: () => Any, services: List[CatalogService] ): Any = {
        blocking {
            val em: EntityManager = emf.createEntityManager()
            TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(em))
            try {
                services.foreach(_.refresh(em))
                task()
            } finally {
                Option(em)
                    .filter(_.isOpen)
                    .foreach(_.close())
                TransactionSynchronizationManager.unbindResource(emf)
            }
        }
    }

}
