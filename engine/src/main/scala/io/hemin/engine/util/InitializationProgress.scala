package io.hemin.engine.util

import com.typesafe.scalalogging.Logger
import io.hemin.engine.HeminException

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

class InitializationProgress (component: Set[String]) {

  private val log = Logger(getClass)
  private val statusMap: mutable.Map[String,Boolean] = mutable.Map(component.toSeq.map { s => (s, false)} : _*)
  private val success: Try[Unit] = Success()

  def signalCompletion(subsystem: String): Try[Unit] =
    statusMap.get(subsystem) match {
      case None        => fail(s"Initialization does not monitor the status of this component : '$subsystem'")
      case Some(true)  => fail(s"Initialization status is already completed for this component : '$subsystem'")
      case Some(false) =>
        log.info(s"${subsystem.toUpperCase} initialization completed ...")
        statusMap += (subsystem -> true)
        success
    }

  def componentCount: Int = component.size

  def isFinished: Boolean = statusMap.foldLeft(true) { case (a, (k,v)) => a && v }

  private def fail(message: String): Failure[Unit] = {
    log.warn(message)
    Failure(new HeminException(message))
  }

}
