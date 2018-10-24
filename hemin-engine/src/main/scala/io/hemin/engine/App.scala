package io.hemin.engine

import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import io.hemin.engine.util.cli.CliProcessor

import scala.concurrent.{ExecutionContext, blocking}
import scala.io.StdIn
import scala.util.{Failure, Success}

object App {

  private val log = Logger(getClass)

  // load and init the configuration
  private val config = ConfigFactory.load(System.getProperty("config.resource", "application.conf"))
  private val engine = new Engine(config)
  private var running = true

  private implicit val ec: ExecutionContext = ExecutionContext.global // TODO anderen als global EC
  private implicit val APPLICATION_TIMEOUT: Timeout = engine.config.node.internalTimeout

  sys.addShutdownHook({
    shutdown("Terminating on SIGTERM")
  })

  def main(args: Array[String]): Unit = {
    log.info("Starting engine ...")
    engine.startup() match {
      case Success(_)  =>
        if (engine.config.node.repl) {
          repl()
        }
      case Failure(ex) => log.error(ex.getMessage)
    }
  }

  private def repl(): Unit = {

    val processor = new CliProcessor(engine.bus, engine.config, ec)
    log.info("CLI is ready to take commands")

    while (running) {
      blocking {
        val input = StdIn.readLine()
        log.debug("REPL input : {}", input)

        Option(input)
          .map(_.split(" "))
          .map(_.toList)
          .foreach {
            case q@("q" | "quit" | "exit") :: _ => running = false
            case cmd => println(processor.eval(cmd))
          }
      }
    }

    shutdown("Terminating on CLI input request")
  }

  private def shutdown(message: String): Unit =
    engine.shutdown() match {
      case Success(_)  => log.info(message)
      case Failure(ex) => log.error(ex.getMessage)
    }
  
}
