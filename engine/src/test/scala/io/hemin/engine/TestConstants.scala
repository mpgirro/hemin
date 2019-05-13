package io.hemin.engine

import java.util.concurrent.Executors

import akka.util.Timeout

import scala.concurrent.ExecutionContext

/**
  * @author max
  */
object TestConstants {
  val engineConfig: HeminConfig = HeminConfig.defaultEngineConfig
  val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  val timeout: Timeout = engineConfig.node.internalTimeout
}
