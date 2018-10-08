package io.disposia.engine

import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import io.disposia.engine.cnc.CliProcessor

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, blocking}
import scala.io.StdIn
import scala.util.{Failure, Success}

object App {

  private implicit val ec: ExecutionContext = ExecutionContext.global // TODO anderen als global EC

  private implicit val INTERNAL_TIMEOUT: Timeout = 5.seconds

  private val log = Logger(getClass)

  private val engine = new Engine()

  private var shutdown = false

  def main(args: Array[String]): Unit = {

    println("Starting engine...")
    engine.start()


    repl() // TODO distinguish between interactive and non-interactive mode

  }

  private def repl(): Unit = {

    val cli = new CliProcessor(engine.bus, engine.config, ec)
    log.info("CLI read to take commands")

    while(!shutdown){
      blocking {
        val input = StdIn.readLine()
        log.debug("CLI read : {}", input)

        Option(input)
          .map(_.split(" "))
          .map(_.toList)
          .foreach { ls: List[String] => ls match {
            case q@("q" | "quit" | "exit") :: _ => shutdown = true
            case others => println(cli.process(others))
          }
          }
      }
    }

    log.info("Terminating due to user request")
    engine.shutdown()
  }

  /*
  @Deprecated
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

  @Deprecated
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

  @Deprecated
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

  @Deprecated
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

  @Deprecated
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
  */

}
