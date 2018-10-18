package io.hemin.engine.index

import scala.concurrent.duration.FiniteDuration

object IndexConfig {
  val dispatcherId: String = "hemin.index.dispatcher"
}

/**
  * Configuration for [[io.hemin.engine.index.IndexStore]]
  */
final case class IndexConfig (
  dispatcherId: String = IndexConfig.dispatcherId,
  luceneIndexPath: String,
  solrUri: String,
  solrQueueSize: Int,
  solrThreadCount: Int,
  createIndex: Boolean,
  commitInterval: FiniteDuration,
  workerCount: Int,
)
