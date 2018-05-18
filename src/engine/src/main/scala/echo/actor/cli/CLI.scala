package echo.actor.cli

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import echo.actor.ActorProtocol._
import echo.actor.directory.DirectoryProtocol._
import echo.core.util.{DocumentFormatter, UrlUtil}

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.blocking
import scala.concurrent.duration._
import scala.io.{Source, StdIn}
import scala.language.postfixOps

/**
  * @author Maximilian Irro
  */

object CLI {
    final val name = "cli"
    def props(master: ActorRef,
              parser: ActorRef,
              searcher: ActorRef,
              crawler: ActorRef,
              directoryStore: ActorRef,
              gateway: ActorRef): Props = {

        Props(new CLI(master, parser, searcher, crawler, directoryStore, gateway))
            .withDispatcher("echo.cli.dispatcher")

    }
}

class CLI(master: ActorRef,
          parser: ActorRef,
          searcher: ActorRef,
          crawler: ActorRef,
          directoryStore: ActorRef,
          gateway: ActorRef) extends Actor with ActorLogging {

    log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

    private val CONFIG = ConfigFactory.load()
    private implicit val INTERNAL_TIMEOUT: Timeout = Option(CONFIG.getInt("echo.internal-timeout")).getOrElse(5).seconds

    private val FEEDS_TXT = "../feeds.txt"
    private val MASSIVE_TXT = "../feeds_unique.txt"

    private var shutdown = false

    val usageMap = Map(
        "propose"        -> "feed [feed [feed]]",
        "check podcast"  -> "[all|<exo>]",
        "check feed"     -> "[all|<exo>]",
        "count"          -> "[podcasts|episodes|feeds]",
        "search"         -> "query [query [query]]",
        "print database" -> "[podcasts|episodes|feeds]",
        "load feeds"     -> "[test|massive]",
        "load fyyd"      -> "[episodes <podcastId> <fyydId>]",
        "save feeds"     -> "<dest>",
        "crawl fyyd"     -> "count",
        "get podcast"    -> "<exo>",
        "get episode"    -> "<exo>"
    )


    // to the REPL, if it terminates, then a poison pill is sent to self and the system will subsequently shutdown too
    repl()

    override def postStop: Unit = {
        log.info("shutting down")
    }

    override def receive: Receive = {
        case unhandled => log.info("Received " + unhandled)
    }

    private def repl() {

        log.info("CLI read to take commands")

        while(!shutdown){
            blocking {
                val input = StdIn.readLine()
                log.debug("CLI read : {}", input)

                Option(input).foreach(i => exec(i.split(" ")))
            }
        }

        log.info("Terminating due to user request")
        master ! ShutdownSystem()
    }

    private def exec(commands: Array[String]): Unit = {
        commands.toList match {
            case "help" :: _ => help()
            case q@("q" | "quit" | "exit") :: _ => shutdown = true

            case "propose" :: Nil   => usage("propose")
            case "propose" :: feeds => feeds.foreach(f => directoryStore ! ProposeNewFeed(f))

            case "check" :: "podcast" :: Nil           => usage("check podcast")
            case "check" :: "podcast" :: "all" :: Nil  => directoryStore ! CheckAllPodcasts
            case "check" :: "podcast" :: "all" :: _    => usage("check podcast")
            case "check" :: "podcast" :: exo :: Nil    => directoryStore ! CheckPodcast(exo)
            case "check" :: "podcast" :: _ :: _        => usage("check podcast")

            case "check" :: "feed" :: Nil              => usage("check feed")
            case "check" :: "feed" :: "all" :: Nil     => directoryStore ! CheckAllFeeds
            case "check" :: "feed" :: "all" :: _       => usage("check feed")
            case "check" :: "feed" :: exo :: Nil       => directoryStore ! CheckFeed(exo)
            case "check" :: "feed" :: _ :: _           => usage("check feed")

            case "count" :: "podcasts" :: Nil => directoryStore ! DebugPrintCountAllPodcasts
            case "count" :: "podcasts" :: _   => usage("count")
            case "count" :: "episodes" :: Nil => directoryStore ! DebugPrintCountAllEpisodes
            case "count" :: "episodes" :: _   => usage("count")
            case "count" :: "feeds" :: Nil    => directoryStore ! DebugPrintCountAllFeeds
            case "count" :: "feeds" :: _      => usage("count")
            case "count" :: _                 => usage("count")

            case "search" :: Nil    => usage("search")
            case "search" :: query  => search(query)

            case "print" :: "database" :: Nil               => usage("print database")
            case "print" :: "database" :: "podcasts" :: Nil => directoryStore ! DebugPrintAllPodcasts
            case "print" :: "database" :: "podcasts" :: _   => usage("print database")
            case "print" :: "database" :: "episodes" :: Nil => directoryStore ! DebugPrintAllEpisodes
            case "print" :: "database" :: "episodes" :: _   => usage("print database")
            case "print" :: "database" :: "feeds" :: Nil    => directoryStore ! DebugPrintAllFeeds
            case "print" :: "database" :: "feeds" :: _      => usage("print database")
            case "print" :: "database" :: _                 => usage("print database")
            case "print" :: _                               => help()

            case "load" :: Nil                         => help()
            case "load" :: "feeds" :: Nil              => usage("load feeds")
            case "load" :: "feeds" :: "test" :: Nil    => loadTestFeeds()
            case "load" :: "feeds" :: "massive" :: Nil => loadMassiveFeeds()
            case "load" :: "feeds" :: _                => usage("load feeds")

            case "load" :: "fyyd" :: "episodes" :: podcastId :: fyydId :: Nil => crawler ! LoadFyydEpisodes(podcastId, fyydId.toLong)
            case "load" :: "fyyd" :: _                                        => usage("load fyyd")

            case "save" :: Nil                    => help()
            case "save" :: "feeds" :: Nil         => usage("save feeds")
            case "save" :: "feeds" :: dest :: Nil => saveFeeds(dest)
            case "save" :: "feeds" :: _           => usage("save feeds")

            case "crawl" :: "fyyd" :: Nil          => usage("crawl fyyd")
            case "crawl" :: "fyyd" :: count :: Nil => crawler ! CrawlFyyd(count.toInt)
            case "crawl" :: "fyyd" :: count :: _   => usage("crawl fyyd")

            case "get" :: "podcast" :: Nil        => usage("get podcast")
            case "get" :: "podcast" :: exo :: Nil => getAndPrintPodcast(exo)
            case "get" :: "podcast" :: exo :: _   => usage("get podcast")

            case "get" :: "episode" :: Nil           => usage("get episode")
            case "get" :: "episode" :: exo :: Nil => getAndPrintEpisode(exo)
            case "get" :: "episode" :: exo :: _   => usage("get episode")

            case _  => help()
        }
    }

    private def usage(cmd: String): Unit = {
        if (usageMap.contains(cmd)) {
            val args = usageMap.get(cmd)
            println("Command parsing error")
            println("Usage: " + cmd + " " + args)
        } else {
            println("Unknown command: " + cmd)
            println("These are the available commands:")
            for ( (k,v) <- usageMap ) {
                println(k + "\t" + v)
            }
        }
    }

    private def help(): Unit = {
        println("This is an interactive REPL providing a CLI to the search engine. Functions are:\n")
        for ( (k,v) <- usageMap ) {
            println(k + "\t" + v)
        }
        println("\nFeel free to play around!\n")
    }

    private def search(query: List[String]): Unit = {
        val future = searcher ? SearchRequest(query.mkString(" "), Some(1), Some(100))
        val response = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[SearchResults]
        response match {
            case SearchResults(results) =>
                println("Found "+results.getResults.size()+" results for query '" + query.mkString(" ") + "'")
                println("Results:")
                for (result <- results.getResults.asScala) {
                    println(s"\n${DocumentFormatter.cliFormat(result)}\n")
                }
                println()
            case other => log.error("Received unexpected DirectoryResult type : {}", other.getClass)
        }
    }

    private def getAndPrintPodcast(exo: String) = {
        val future = directoryStore ? GetPodcast(exo)
        val response = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[DirectoryQueryResult]
        response match {
            case PodcastResult(podcast)  => println(podcast.toString)
            case NothingFound(unknownId) => log.info("DirectoryStore responded that there is no Podcast with EXO : {}", unknownId)
            case other                   => log.error("Received unexpected DirectoryResult type : {}", other.getClass)
        }
    }

    private def getAndPrintEpisode(exo: String) = {
        val future = directoryStore ? GetEpisode(exo)
        val response = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[DirectoryQueryResult]
        response match {
            case EpisodeResult(episode)  => println(episode.toString)
            case NothingFound(unknownId) => log.info("DirectoryStore responded that there is no Episode with EXO : {}", unknownId)
            case other                   => log.error("Received unexpected DirectoryResult type : {}", other.getClass)
        }
    }

    private def loadTestFeeds(): Unit = {
        log.debug("Received LoadTestFeeds")
        for (feed <- Source.fromFile(FEEDS_TXT).getLines) {
            directoryStore ! ProposeNewFeed(UrlUtil.sanitize(feed))
        }
    }

    private def loadMassiveFeeds(): Unit = {
        log.debug("Received LoadMassiveFeeds")
        for (feed <- Source.fromFile(MASSIVE_TXT).getLines) {
            directoryStore ! ProposeNewFeed(UrlUtil.sanitize(feed))
        }
    }

    private def saveFeeds(dest: String): Unit = {
        log.debug("Received SaveFeeds : {}", dest)

        val future = directoryStore ? GetAllFeeds(0, 10000)
        val response = Await.result(future, INTERNAL_TIMEOUT.duration).asInstanceOf[DirectoryQueryResult]
        response match {
            case AllFeedsResult(feeds)  =>
                import java.io._
                val pw = new PrintWriter(new File(dest))
                log.info("Writing {} feeds to file : {}", feeds.size, dest)
                feeds.foreach(f => pw.write(f.getUrl+ "\n"))
                pw.close()
            case other => log.error("Received unexpected DirectoryResult type : {}", other.getClass)
        }
    }

}
