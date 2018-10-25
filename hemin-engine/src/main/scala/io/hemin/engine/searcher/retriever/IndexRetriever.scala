package io.hemin.engine.searcher.retriever

import com.google.common.base.Strings.isNullOrEmpty
import io.hemin.engine.model.ResultPage
import io.hemin.engine.searcher.SearcherConfig

import scala.concurrent.{ExecutionContext, Future}

trait IndexRetriever {

  protected[this] implicit def executionContext: ExecutionContext

  protected[this] def searcherConfig: SearcherConfig

  protected[this] def searchIndex(q: String, p: Int, s: Int): ResultPage

  final def search(query: String, page: Option[Int], size: Option[Int]): Future[ResultPage] = {
    val p: Int = page.getOrElse(searcherConfig.defaultPage)
    val s: Int = size.getOrElse(searcherConfig.defaultSize)
    search(query, p, s)
  }

  final def search(query: String, page: Int, size: Int): Future[ResultPage] = Future {
    if (isNullOrEmpty(query) || page < 1 || size < 1) {
      ResultPage.empty
    } else {
      searchIndex(query, page, size)
    }
  }

}
