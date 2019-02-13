package hemin.engine.searcher.retriever

import hemin.engine.model.SearchResult
import hemin.engine.searcher.SearcherConfig

import scala.concurrent.ExecutionContext


class ElasticRetriever (config: SearcherConfig,
                        ec: ExecutionContext)
  extends IndexRetriever {

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] def searcherConfig: SearcherConfig = config

  override protected[this] def searchIndex(q: String, p: Int, s: Int): SearchResult = {
    // TODO implement!
    throw new UnsupportedOperationException("ElasticRetriever.searchIndex(_,_,_) not yet implemented")
  }

}
