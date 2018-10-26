package io.hemin.engine

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, blocking}
import scala.io.StdIn
import scala.util.{Failure, Success}

/** Main entry point to use a [[io.hemin.engine.Engine]] in standalone mode */
object EngineApp extends App {

  private val log = Logger(getClass)

  // load and init the configuration
  private lazy val config = ConfigFactory.load(System.getProperty("config.resource", "application.conf"))
  private lazy val engine = new Engine(config)
  //private lazy val ec: ExecutionContext = engine.system.dispatchers.lookup(engine.config.node.dispatcher)
  private implicit lazy val ec: ExecutionContext = ExecutionContext.global // TODO

  private var running = true

  private def shutdown(message: String): Unit =
    engine.shutdown() match {
      case Success(_)  => log.info(message)
      case Failure(ex) => log.error(ex.getMessage)
    }

  private def repl(ec: ExecutionContext): Unit = {

    val processor = engine.cliProcessor(ec)
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


  sys.addShutdownHook({
    shutdown("Terminating on SIGTERM")
  })


  if (engine.config.node.repl) {
    repl(ec)
  }

}
