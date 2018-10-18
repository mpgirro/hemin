package io.hemin.engine.index

import scala.concurrent.duration.FiniteDuration

object IndexConfig {
  val dispatcher: String = "hemin.index.dispatcher"
}

/** Configuration for [[io.hemin.engine.index.IndexStore]] */
final case class IndexConfig (
  luceneIndexPath: String,
  solrUri: String,
  solrQueueSize: Int,
  solrThreadCount: Int,
  createIndex: Boolean,
  commitInterval: FiniteDuration,
  workerCount: Int,
) {
  val dispatcher: String = IndexConfig.dispatcher
  val mailbox: String = IndexStorePriorityMailbox.name
}
