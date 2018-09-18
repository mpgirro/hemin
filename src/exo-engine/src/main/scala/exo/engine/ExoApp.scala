package exo.engine

import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import exo.engine.util.DocumentFormatter

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, blocking}
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps
import scala.util.{Failure, Success}

object ExoApp {

    private implicit val ec: ExecutionContext = ExecutionContext.global // TODO anderen als global EC

    private implicit val INTERNAL_TIMEOUT: Timeout = 5.seconds

    private val log = Logger(classOf[App])

    private val engine = new ExoEngine()
    private var shutdown = false

    private val usageMap = Map(
        "propose"        -> "feed [feed [feed]]",
        "benchmark"      -> "<feed|index|search>",
        "benchmark feed" -> "feed <url>",
        "benchmark index"-> "",
        "benchmark search"-> "",
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
        "get episode"    -> "<exo>",
        "request mean episodes" -> ""
    )

    def main(args: Array[String]): Unit = {

        println("Starting engine...")
        engine.start()


        repl() // TODO distinguish between interactive and non-interactive mode

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
        engine.shutdown()
    }

    private def exec(commands: Array[String]): Unit = {
        commands.toList match {
            case "help" :: _ => help()
            case q@("q" | "quit" | "exit") :: _ => shutdown = true

            case "propose" :: Nil   => usage("propose")
            case "propose" :: feeds => feeds.foreach(f => engine.propose(f))

            case "search" :: Nil          => usage("search")
            case "search" :: query :: Nil => search(query)
            //case "search" :: query :: _   => usage("search")

            case "get" :: "podcast" :: Nil        => usage("get podcast")
            case "get" :: "podcast" :: exo :: Nil => getPodcast(exo)
            case "get" :: "podcast" :: exo :: _   => usage("get podcast")

            case "get" :: "episode" :: Nil        => usage("get episode")
            case "get" :: "episode" :: exo :: Nil => getEpisode(exo)
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

    private def search(query: String): Unit = {
        engine
            .search(query, 1, 20)
            .onComplete {
                case Success(results) =>
                    println("Found "+results.getResults.size()+" results for query '" + query.mkString(" ") + "'")
                    println("Results:")
                    for (result <- results.getResults.asScala) {
                        println(s"\n${DocumentFormatter.cliFormat(result)}\n")
                    }
                    println()
                case Failure(reason)  => println("ERROR: " + reason.getMessage)
            }
    }

    private def getPodcast(exo: String): Unit = {
        engine
            .findPodcast(exo)
            .onComplete {
                case Success(result) =>
                    result match {
                        case Some(p) => println(p.toString)
                        case None    => println("Unknown EXO")
                    }
                case Failure(reason)  => println("ERROR: " + reason.getMessage)
            }
    }

    private def getEpisode(exo: String): Unit = {
        engine
            .findEpisode(exo)
            .onComplete {
                case Success(result) =>
                    result match {
                        case Some(e) => println(e.toString)
                        case None    => println("Unknown EXO")
                    }
                case Failure(reason)  => println("ERROR: " + reason.getMessage)
            }
    }

}
