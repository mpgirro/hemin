package io.disposia.engine.index

import scala.concurrent.duration.FiniteDuration

/**
  * Configuration for [[io.disposia.engine.index.IndexStore]]
  */
case class IndexConfig (
  indexPath: String,
  createIndex: Boolean,
  commitInterval: FiniteDuration,
  workerCount: Int,
  defaultPage: Int,
  defaultSize: Int
)
