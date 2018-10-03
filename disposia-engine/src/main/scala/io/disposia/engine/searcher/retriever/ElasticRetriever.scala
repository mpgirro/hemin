package io.disposia.engine.searcher.retriever
import io.disposia.engine.domain.ResultWrapper
import io.disposia.engine.index.IndexConfig

import scala.concurrent.ExecutionContext


class ElasticRetriever (config: IndexConfig, ec: ExecutionContext) extends IndexRetriever {

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] def searchIndex(q: String, p: Int, s: Int): ResultWrapper = {
    // TODO implement!
    throw new UnsupportedOperationException("ElasticRetriever.search(_,_,_) not yet implemented")
  }

}
