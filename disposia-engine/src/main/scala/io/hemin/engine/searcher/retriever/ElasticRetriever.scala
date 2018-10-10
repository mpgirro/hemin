package io.hemin.engine.searcher.retriever

import io.hemin.engine.index.IndexConfig
import io.hemin.engine.domain.ResultsWrapper

import scala.concurrent.ExecutionContext


class ElasticRetriever (config: IndexConfig, ec: ExecutionContext) extends IndexRetriever {

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] def searchIndex(q: String, p: Int, s: Int): ResultsWrapper = {
    // TODO implement!
    throw new UnsupportedOperationException("ElasticRetriever.search(_,_,_) not yet implemented")
  }

}
