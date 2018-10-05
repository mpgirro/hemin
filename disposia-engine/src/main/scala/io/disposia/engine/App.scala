package io.disposia.engine

import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import io.disposia.engine.util.DocumentFormatter

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, blocking}
import scala.io.StdIn
import scala.util.{Failure, Success}

object App {

  private implicit val ec: ExecutionContext = ExecutionContext.global // TODO anderen als global EC

  private implicit val INTERNAL_TIMEOUT: Timeout = 5.seconds

  private val log = Logger(classOf[App])

  private val engine = new Engine()
  private var shutdown = false

  private val usageMap = Map(
    "propose"        -> "feed [feed [feed]]",
    "benchmark"      -> "<feed|index|search>",
    "benchmark feed" -> "feed <url>",
    "benchmark index"-> "",
    "benchmark search"-> "",
    "check podcast"  -> "[all|<id>]",
    "check feed"     -> "[all|<id>]",
    "count"          -> "[podcasts|episodes|feeds]",
    "search"         -> "query [query [query]]",
    "print database" -> "[podcasts|episodes|feeds]",
    "load feeds"     -> "[test|massive]",
    "load fyyd"      -> "[episodes <podcastId> <fyydId>]",
    "save feeds"     -> "<dest>",
    "crawl fyyd"     -> "count",
    "get podcast"    -> "<id>",
    "get episode"    -> "<id>",
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

      case "get" :: "podcast" :: Nil       => usage("get podcast")
      case "get" :: "podcast" :: id :: Nil => printPodcast(id)
      case "get" :: "podcast" :: id :: _   => usage("get podcast")

      case "get" :: "podcast-feeds" :: Nil       => usage("get podcast")
      case "get" :: "podcast-feeds" :: id :: Nil => printFeedsByPodcast(id)
      case "get" :: "podcast-feeds" :: id :: _   => usage("get podcast")

      case "get" :: "episode" :: Nil       => usage("get episode")
      case "get" :: "episode" :: id :: Nil => printEpisode(id)
      case "get" :: "episode" :: id :: _   => usage("get episode")

      case "get" :: "episode-chapters" :: Nil       => usage("get chapters")
      case "get" :: "episode-chapters" :: id :: Nil => printChaptersByEpisode(id)
      case "get" :: "episode-chapters" :: id :: _   => usage("get chapters")

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
          println("Found "+results.results.length+" results for query '" + query.mkString(" ") + "'")
          println("Results:")
          for (result <- results.results) {
            //println(s"\n${DocumentFormatter.cliFormat(result)}\n") // TODO port the cliFormatter to new Format
            println(s"\n${result}\n")
          }
          println()
        case Failure(reason)  => println("ERROR: " + reason.getMessage)
      }
  }

  private def printPodcast(id: String): Unit = {
    engine
      .findPodcast(id)
      .onComplete {
        case Success(result) =>
          result match {
            case Some(p) => println(p.toString)
            case None    => println("Unknown ID")
          }
        case Failure(reason)  => println("ERROR: " + reason.getMessage)
      }
  }

  private def printEpisode(id: String): Unit = {
    engine
      .findEpisode(id)
      .onComplete {
        case Success(result) =>
          result match {
            case Some(e) => println(e.toString)
            case None    => println("Unknown ID")
          }
        case Failure(reason)  => println("ERROR: " + reason.getMessage)
      }
  }

  private def printChaptersByEpisode(id: String): Unit = {
    engine
      .findChaptersByEpisode(id)
      .onComplete {
        case Success(cs) =>
          if (cs.isEmpty)
            println("No chapters found")
          else
            for (c <- cs) println(c.title)
        case Failure(reason) => println("ERROR: " + reason.getMessage)
      }
  }

  private def printFeedsByPodcast(id: String): Unit = {
    engine
      .findFeedsByPodcast(id)
      .onComplete {
        case Success(fs) =>
          if (fs.isEmpty)
            println("No feeds found")
          else
            for (f <- fs) println(f.url)
        case Failure(reason) => println("ERROR: " + reason.getMessage)
      }
  }

}
