package io.disposia.engine

import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import io.disposia.engine.cnc.ReplProcessor

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, blocking}
import scala.io.StdIn

object App {

  private implicit val ec: ExecutionContext = ExecutionContext.global // TODO anderen als global EC
  private implicit val INTERNAL_TIMEOUT: Timeout = 5.seconds

  private val log = Logger(getClass)
  private val engine = new Engine()
  private var running = true

  def main(args: Array[String]): Unit = {
    log.info("Starting engine...")
    engine.start()
    repl() // TODO distinguish between interactive and non-interactive mode
  }

  private def repl(): Unit = {

    val repl = new ReplProcessor(engine.bus, engine.config, ec)
    log.info("CLI is ready to take commands")

    while(running){
      blocking {
        val input = StdIn.readLine()
        log.debug("CLI read : {}", input)

        Option(input)
          .map(_.split(" "))
          .map(_.toList)
          .foreach {
            case q@("q" | "quit" | "exit") :: _ => running = false
            case others => println(repl.process(others))
          }
      }
    }

    log.info("Terminating due to user request")
    engine.shutdown()
  }

}
