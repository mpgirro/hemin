package io.hemin.engine.searcher.retriever

import com.google.common.base.Strings.isNullOrEmpty
import io.hemin.engine.model.ResultPage
import io.hemin.engine.searcher.SearcherConfig

import scala.concurrent.{ExecutionContext, Future}

trait IndexRetriever {

  protected[this] implicit def executionContext: ExecutionContext

  protected[this] def searcherConfig: SearcherConfig

  protected[this] def searchIndex(q: String, p: Int, s: Int): ResultPage

  /** Searches the reverse index for the given query. Returns an eventual
    * [[io.hemin.engine.model.ResultPage]] for the page and size parameters.
    *
    * @param query The query to search the reverse index for.
    * @param page  The page for the [[io.hemin.engine.model.ResultPage]]. If None, then
    *              [[io.hemin.engine.searcher.SearcherConfig.defaultPage]] is used.
    * @param size  The size (= maximum number of elements in the
    *              [[io.hemin.engine.model.ResultPage.results]] list) of the
    *              [[io.hemin.engine.model.ResultPage]]. If None, then
    *              [[io.hemin.engine.searcher.SearcherConfig.defaultSize]] is used.
    * @return The [[io.hemin.engine.model.ResultPage]] matching the query/page/size parameters.
    */
  final def search(query: String, page: Option[Int], size: Option[Int]): Future[ResultPage] = {
    val p: Int = page.getOrElse(searcherConfig.defaultPage)
    val s: Int = size.getOrElse(searcherConfig.defaultSize)
    search(query, p, s)
  }

  /** Searches the reverse index for the given query. Returns an eventual
    * [[io.hemin.engine.model.ResultPage]] for the page and size parameters.
    *
    * @param query The query to search the reverse index for.
    * @param page  The page for the [[io.hemin.engine.model.ResultPage]].
    * @param size  The size (= maximum number of elements in the [[io.hemin.engine.model.ResultPage.results]]
    *              list) of the [[io.hemin.engine.model.ResultPage]].
    * @return The [[io.hemin.engine.model.ResultPage]] matching the query/page/size parameters.
    */
  final def search(query: String, page: Int, size: Int): Future[ResultPage] = Future {
    if (isNullOrEmpty(query) || page < 1 || size < 1) {
      ResultPage.empty
    } else {
      searchIndex(query, page, size)
    }
  }

}
