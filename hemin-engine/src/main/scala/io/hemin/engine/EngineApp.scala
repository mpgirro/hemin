package io.hemin.engine

import java.util.concurrent.atomic.AtomicBoolean

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, blocking}
import scala.io.StdIn
import scala.util.{Failure, Success}

/** Main entry point to use a [[io.hemin.engine.Engine]] in standalone mode */
object EngineApp extends App {

  private val log = Logger(getClass)

  // exit status code of the program upon termination
  private lazy val STATUS_SUCCESS: Int = 0
  private lazy val STATUS_ERROR: Int = -1

  // load the configuration and startup the engine
  private lazy val config: Config = ConfigFactory.load(System.getProperty("config.resource", "application.conf"))
  private lazy val engine: Engine = Engine.of(config) match {
    case Success(e)  => e
    case Failure(ex) =>
      shutdown(s"Terminating due failed Engine initialization; reason : ${ex.getMessage}")
      null // TODO can I return a better result value (just to please the compiler?)
  }
  private lazy val running: AtomicBoolean = new AtomicBoolean(true)

  private implicit lazy val ec: ExecutionContext = ExecutionContext.global

  private def shutdown(message: String): Unit = {
    val status: Int = Option(engine)
      .map { _
        .shutdown() match {
          case Success(_) =>
            log.info(message)
            STATUS_SUCCESS
          case Failure(ex) =>
            log.error(ex.getMessage)
            ex.printStackTrace()
            STATUS_ERROR
        }
      }.getOrElse(STATUS_ERROR)
    System.exit(status)
  }

  private def repl(ec: ExecutionContext): Unit = {
    log.info("CLI is ready to take commands")
    while (running.get) {
      blocking {
        val input = StdIn.readLine()
        log.debug("REPL input : {}", input)

        Option(input)
          .map(_.split(" "))
          .map(_.toList)
          .foreach {
            case _@("q" | "quit" | "exit") :: _ => running.set(false)
            case _ => engine.cli(input).onComplete(println)(ec)
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
