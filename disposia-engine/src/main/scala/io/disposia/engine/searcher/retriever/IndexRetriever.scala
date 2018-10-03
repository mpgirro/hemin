package io.disposia.engine.searcher.retriever

import com.google.common.base.Strings.isNullOrEmpty
import io.disposia.engine.domain.ResultWrapper

import scala.concurrent.{ExecutionContext, Future}

trait IndexRetriever {

  protected[this] implicit def executionContext: ExecutionContext

  protected[this] def searchIndex(q: String, p: Int, s: Int): ResultWrapper

  final def search(query: String, page: Int, size: Int): Future[ResultWrapper] = Future {
    if (isNullOrEmpty(query) || page < 1 || size < 1) {
      ResultWrapper.empty()
    } else {
      searchIndex(query,page,size)
    }
  }

}
