package io.disposia.engine.searcher.retriever

import com.google.common.base.Strings.isNullOrEmpty
import io.disposia.engine.domain.ResultsWrapper

import scala.concurrent.{ExecutionContext, Future}

trait IndexRetriever {

  protected[this] implicit def executionContext: ExecutionContext

  protected[this] def searchIndex(q: String, p: Int, s: Int): ResultsWrapper

  final def search(query: String, page: Int, size: Int): Future[ResultsWrapper] = Future {
    if (isNullOrEmpty(query) || page < 1 || size < 1) {
      ResultsWrapper()
    } else {
      searchIndex(query,page,size)
    }
  }

}
