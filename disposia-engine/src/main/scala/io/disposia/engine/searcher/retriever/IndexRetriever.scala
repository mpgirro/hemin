package io.disposia.engine.searcher.retriever

import com.google.common.base.Strings.isNullOrEmpty
import io.disposia.engine.newdomain.NewResults

import scala.concurrent.{ExecutionContext, Future}

trait IndexRetriever {

  protected[this] implicit def executionContext: ExecutionContext

  protected[this] def searchIndex(q: String, p: Int, s: Int): NewResults

  final def search(query: String, page: Int, size: Int): Future[NewResults] = Future {
    if (isNullOrEmpty(query) || page < 1 || size < 1) {
      NewResults()
    } else {
      searchIndex(query,page,size)
    }
  }

}
