package io.hemin.engine.searcher

/**
  * Configuration for [[io.hemin.engine.searcher.Searcher]]
  */
final case class SearcherConfig (
  solrUri: String,
  defaultPage: Int,
  defaultSize: Int
)
