package exo.engine.config

import scala.concurrent.duration.FiniteDuration

/**
  * Configuration for [[exo.engine.index.IndexStore]]
  */
case class IndexConfig (
    indexPath: String,
    createIndex: Boolean,
    commitInterval: FiniteDuration,
    workerCount: Int
)
