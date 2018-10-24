package io.hemin.engine

import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import io.hemin.engine.util.cli.CliProcessor

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, blocking}
import scala.io.StdIn

object App {

  private implicit val ec: ExecutionContext = ExecutionContext.global // TODO anderen als global EC
  private implicit val APPLICATION_TIMEOUT: Timeout = 5.seconds

  private val log = Logger(getClass)

  // load and init the configuration
  private val config = ConfigFactory.load(System.getProperty("config.resource", "application.conf"))
  private val engine = new Engine(config)
  private var running = true

  def main(args: Array[String]): Unit = {
    log.info("Starting engine ...")
    engine.start()
    if (engine.config.node.repl) {
      repl()
    }
  }

  private def repl(): Unit = {

    val processor = new CliProcessor(engine.bus, engine.config, ec)
    log.info("CLI is ready to take commands")

    while (running) {
      blocking {
        val input = StdIn.readLine()
        log.debug("CLI read : {}", input)

        Option(input)
          .map(_.split(" "))
          .map(_.toList)
          .foreach {
            case q@("q" | "quit" | "exit") :: _ => running = false
            case cmd => println(processor.eval(cmd))
          }
      }
    }

    log.info("Terminating due to user request")
    engine.shutdown()
  }

  sys.addShutdownHook({
    log.debug("App ShutdownHook called")
    engine.shutdown()
  })

}
