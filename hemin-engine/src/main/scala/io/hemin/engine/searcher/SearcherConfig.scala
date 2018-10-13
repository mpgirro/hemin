package io.hemin.engine.searcher

object SearcherConfig {
  val dispatcherId: String = "hemin.searcher.dispatcher"
}

/**
  * Configuration for [[io.hemin.engine.searcher.Searcher]]
  */
final case class SearcherConfig (
  dispatcherId: String = dispatcherId.executionContextId,
  solrUri: String,
  defaultPage: Int,
  defaultSize: Int,
)
