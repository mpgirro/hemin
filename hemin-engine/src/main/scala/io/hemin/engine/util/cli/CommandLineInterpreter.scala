package io.hemin.engine.util.cli

import java.io.ByteArrayOutputStream

import akka.actor.ActorRef
import com.typesafe.scalalogging.Logger
import io.hemin.engine.HeminConfig
import io.hemin.engine.util.cli.CommandLineInterpreter.CliAction

import scala.concurrent.ExecutionContext

object CommandLineInterpreter {
  type CliAction = (CliParams) => Unit
}

class CommandLineInterpreter (bus: ActorRef,
                              config: HeminConfig,
                              ec: ExecutionContext) {

  private val log = Logger(getClass)

  private val parser: CliParser = new CliParser
  private val processor: CliProcessor = new CliProcessor(bus, config, ec)

  def execute(input: String): String = {
    val params: Option[CliParams] = parser.parse(input)
    val action: Option[CliAction] = params.flatMap(p => processor.eval(p))
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
      case (None, _) => "Unknown command"
    }
  }

}
