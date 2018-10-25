package io.hemin.engine

import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.{CircuitBreaker, ask}
import akka.util.Timeout
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger
import io.hemin.engine.EngineProtocol._
import io.hemin.engine.Node.{CliInput, CliOutput}
import io.hemin.engine.catalog.CatalogStore._
import io.hemin.engine.domain._
import io.hemin.engine.exception.HeminException
import io.hemin.engine.searcher.Searcher.{SearcherRequest, SearcherResults}
import io.hemin.engine.util.cli.CliProcessor

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object Engine {
  val name: String = "hemin"
}

class Engine (private val initConfig: Config) {

  private val completeConfig: Config = initConfig.withFallback(EngineConfig.defaultConfig)

  private val log = Logger(getClass)
  private val engineConfig: EngineConfig = EngineConfig.load(completeConfig)
  private val nodeConfig: NodeConfig = engineConfig.node

  private implicit lazy val internalTimeout: Timeout = nodeConfig.internalTimeout
  private implicit lazy val executionContext: ExecutionContext = system.dispatchers.lookup(nodeConfig.dispatcher)

  // lazy init the actor system and local bus for this node
  private[engine] lazy val system: ActorSystem = ActorSystem(Engine.name, completeConfig)
  private[engine] lazy val bus: ActorRef = system.actorOf(Props(new Node(engineConfig)), Node.name)

  private lazy val circuitBreaker = CircuitBreaker(system.scheduler, nodeConfig.breakerMaxFailures, nodeConfig.breakerCallTimeout.duration, nodeConfig.breakerResetTimeout.duration)
    .onOpen(breakerOpen())
    .onClose(breakerClose())
    .onHalfOpen(breakerHalfOpen())

  private var running: AtomicBoolean = new AtomicBoolean(false)

  // Run the startup sequence. This will throw an exception in case a Failure occurred
  startupSequence() match {
    case Success(_)  => log.info("ENGINE startup complete ...")
    case Failure(ex) => throw ex
  }

  /** Attempts to shutdown the Engine. This operation is thread-safe.
    * It will produce a `Failure` if the Engine is not running. */
  def shutdown(): Try[Unit] = synchronized { if (running.get()) shutdownOnWarm() else shutdownOnCold() }

  /** Configuration of the Engine instance */
  def config: EngineConfig = engineConfig

  def propose(url: String): Unit = guarded {
    bus ! ProposeNewFeed(url)
  }

  def cli(args: String): Future[String] = guarded {
    (bus ? CliInput(args)).map {
      case CliOutput(txt) => txt
    }
  }

  def search(query: String, page: Option[Int], size: Option[Int]): Future[ResultsWrapper] = guarded {
    (bus ? SearcherRequest(query, page, size)).map {
      case SearcherResults(rs) => rs
    }
  }

  def findPodcast(id: String): Future[Option[Podcast]] = guarded {
    (bus ? GetPodcast(id)).map {
      case PodcastResult(p) => p
    }
  }

  def findEpisode(id: String): Future[Option[Episode]] = guarded {
    (bus ? GetEpisode(id)).map {
      case EpisodeResult(e) => e
    }
  }

  def findFeed(id: String): Future[Option[Feed]] = guarded {
    (bus ? GetFeed(id)).map {
      case FeedResult(f) => f
    }
  }

  def findImage(id: String): Future[Option[Image]] = guarded {
    (bus ? GetImage(id)).map {
      case ImageResult(i) => i
    }
  }

  def findImageByAssociate(id: String): Future[Option[Image]] = guarded {
    (bus ? GetImageByAssociate(id)).map {
      case ImageResult(i) => i
    }
  }

  def findAllPodcasts(page: Option[Int], size: Option[Int]): Future[List[Podcast]] = guarded {
    (bus ? GetAllPodcastsRegistrationComplete(page, size)).map {
      case AllPodcastsResult(ps) => ps
    }
  }

  def findEpisodesByPodcast(id: String): Future[List[Episode]] = guarded {
    (bus ? GetEpisodesByPodcast(id)).map {
      case EpisodesByPodcastResult(es) => es
    }
  }

  def findFeedsByPodcast(id: String): Future[List[Feed]] = guarded {
    (bus ? GetFeedsByPodcast(id)).map {
      case FeedsByPodcastResult(fs) => fs
    }
  }

  def findChaptersByEpisode(id: String): Future[List[Chapter]] = guarded {
    (bus ? GetChaptersByEpisode(id)).map {
      case ChaptersByEpisodeResult(cs) => cs
    }
  }

  /** Returns a new `CliProcessor` instance, that runs on the provided ExecutionContext
    *
    * @param ec ExecutionContext that the CliProcessor is running on
    * @return new CliProcessor instance
    */
  def cliProcessor(ec: ExecutionContext): CliProcessor = new CliProcessor(bus, config, ec)

  /** the call to warmup() will tap the lazy values, and wait until all
    * actors in the hierarchy report that they are up and running */
  private def startupSequence(): Try[Unit] = synchronized {
    log.info("ENGINE is starting up ...")
    warmup()
  }

  private def warmup(): Try[Unit] =
    Await.result(bus ? EngineOperational, internalTimeout.duration) match {
      case StartupComplete =>
        running.set(true)
        Success(Unit)
      case StartupInProgress =>
        Thread.sleep(25) // don't wait too busy
        warmup()
    }

  private def shutdownOnWarm(): Try[Unit] = {
    log.info("ENGINE is shutting down ...")
    //bus ! ShutdownSystem // TODO does system.terminate() work better?
    system.terminate()
    running.set(false)
    Success(Unit)
  }

  private def shutdownOnCold(): Try[Unit] = Failure(new HeminException("Engine shutdown failed; reason: not running"))

  private def guarded[T](body: => Future[T]): Future[T] =
    if (running.get()) {
      circuitBreaker.withCircuitBreaker(body)
    } else {
      Future.failed(notRunningException)
    }

  private def guarded[T](body: => T): T =
    if (running.get()) {
      circuitBreaker.withSyncCircuitBreaker(body)
    } else {
      throw notRunningException
    }

  private def notRunningException: HeminException = new HeminException("Guard prevented call; reason: Engine not running")

  private def breakerOpen(): Unit = log.info("Circuit Breaker is open")

  private def breakerClose(): Unit = log.warn("Circuit Breaker is closed")

  private def breakerHalfOpen(): Unit = log.info("Circuit Breaker is half-open, next message goes through")

}
