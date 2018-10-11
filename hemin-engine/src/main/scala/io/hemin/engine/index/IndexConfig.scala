package io.hemin.engine.index

import scala.concurrent.duration.FiniteDuration

/**
  * Configuration for [[io.hemin.engine.index.IndexStore]]
  */
final case class IndexConfig (
  luceneIndexPath: String,
  solrUri: String,
  solrQueueSize: Int,
  solrThreadCount: Int,
  createIndex: Boolean,
  commitInterval: FiniteDuration,
  workerCount: Int,
  defaultPage: Int,
  defaultSize: Int
)
