package io.hemin.engine.searcher

object SearcherConfig {
  val dispatcher: String = "hemin.searcher.dispatcher"
}

/** Configuration for [[io.hemin.engine.searcher.Searcher]] */
final case class SearcherConfig (
  solrUri: String,
  defaultPage: Int,
  defaultSize: Int,
) {
  val dispatcher: String = SearcherConfig.dispatcher
  val mailbox: String = SearcherPriorityMailbox.name
}
