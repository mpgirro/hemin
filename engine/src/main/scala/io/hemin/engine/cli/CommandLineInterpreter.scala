package io.hemin.engine.cli

import java.io.ByteArrayOutputStream

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.Logger
import io.hemin.engine.HeminConfig
import io.hemin.engine.cli.CommandLineInterpreter.{CliAction, InterpreterInput, InterpreterOutput}
import io.hemin.engine.node.Node.{ActorRefSupervisor, ReportCliInitializationComplete}

import scala.concurrent.ExecutionContext

object CommandLineInterpreter {
  type CliAction = (CliParams) => Unit

  final val name = "cli"

  def props(config: HeminConfig): Props =
    Props(new CommandLineInterpreter(config))
      .withDispatcher(config.cli.dispatcher)
      .withMailbox(config.cli.mailbox)

  trait CliMessage
  trait CliQuery extends CliMessage
  trait CliQueryResult extends CliMessage

  // CliQuery
  final case class InterpreterInput(input: String) extends CliQuery

  // CliQueryResult
  final case class InterpreterOutput(output: String) extends CliQueryResult
}

class CommandLineInterpreter (config: HeminConfig)
  extends Actor {

  private val log = Logger(getClass)

  private implicit val executionContext: ExecutionContext = context.dispatcher

  private val parser: CliParser = new CliParser

  private var supervisor: ActorRef = _
  private var bus: ActorRef = _
  private var processor: CliProcessor = _

  override def postRestart(cause: Throwable): Unit = {
    log.warn("{} has been restarted or resumed", self.path.name)
    super.postRestart(cause)
  }

  override def postStop(): Unit = {
    log.info("{} subsystem shutting down", CommandLineInterpreter.name.toUpperCase)
  }

  override def receive: Receive = {

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      bus = ref
      processor = new CliProcessor(bus, context.system, config, executionContext)
      supervisor ! ReportCliInitializationComplete

    case InterpreterInput(input) => onInterpreterInput(input)
  }

  private def onInterpreterInput(input: String): Unit = {
    log.debug("Received InterpreterInput('{}')", input)
    val output = execute(input)
    sender ! InterpreterOutput(output)
  }

  private def execute(input: String): String = {
    val params: Option[CliParams] = parser.parse(input)
    val action: Option[CliAction] = params.flatMap(processor.eval)
    (params, action) match {
      case (Some(p), Some(a)) =>
        val out = new ByteArrayOutputStream
        Console.withOut(out) {
          Console.withErr(out) {
            //println(s"CLI parser results : ${params.summary}")
            a(p) // apply action to params
          }
        }
        out.toString
      case (_, _) => "Unknown command"
    }
  }

}
