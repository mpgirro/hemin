package io.hemin.engine.util

import com.typesafe.scalalogging.Logger

import scala.collection.mutable

class InitializationProgress (subsystems: Seq[String]) {

  private val log = Logger(getClass)
  private val progress: mutable.Map[String,Boolean] = mutable.Map(subsystems.map {s => (s, false)} : _*)

  def complete(subsystem: String): Unit =
    if (progress.contains(subsystem)) {
      log.info(s"${subsystem.toUpperCase} subsystem initialized ...")
      progress += (subsystem -> true)
    } else {
      log.error("Initialization does no monitor the progress of this subsystem : " + subsystem)
    }

  def isFinished: Boolean = progress.foldLeft(true) { case (a, (k,v)) => a && v }

}
