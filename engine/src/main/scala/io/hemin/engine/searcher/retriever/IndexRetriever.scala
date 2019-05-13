package io.hemin.engine.searcher.retriever

import com.google.common.base.Strings.isNullOrEmpty
import io.hemin.engine.model.SearchResult
import io.hemin.engine.searcher.SearcherConfig

import scala.concurrent.{ExecutionContext, Future}

trait IndexRetriever {

  protected[this] implicit def executionContext: ExecutionContext

  protected[this] def searcherConfig: SearcherConfig

  protected[this] def searchIndex(q: String, p: Int, s: Int): SearchResult

  /** Searches the reverse index for the given query. Returns an eventual
    * [[io.hemin.engine.model.SearchResult]] for the page and size parameters.
    *
    * @param query      The query to search the reverse index for.
    * @param pageNumber The page for the [[io.hemin.engine.model.SearchResult]]. If None, then
    *                   [[io.hemin.engine.searcher.SearcherConfig.defaultPage]] is used.
    * @param pageSize   The size (= maximum number of elements in the
    *                   [[io.hemin.engine.model.SearchResult.results]] list) of the
    *                   [[io.hemin.engine.model.SearchResult]]. If None, then
    *                   [[io.hemin.engine.searcher.SearcherConfig.defaultSize]] is used.
    * @return The [[io.hemin.engine.model.SearchResult]] matching the query/page/size parameters.
    */
  final def search(query: String, pageNumber: Option[Int], pageSize: Option[Int]): Future[SearchResult] = {
    val p: Int = pageNumber.getOrElse(searcherConfig.defaultPage)
    val s: Int = pageSize.getOrElse(searcherConfig.defaultSize)
    search(query, p, s)
  }

  /** Searches the reverse index for the given query. Returns an eventual
    * [[io.hemin.engine.model.SearchResult]] for the page and size parameters.
    *
    * @param query      The query to search the reverse index for.
    * @param pageNumber The page for the [[io.hemin.engine.model.SearchResult]].
    * @param pageSize   The size (= maximum number of elements in the
    *                   [[io.hemin.engine.model.SearchResult.results]] list) of
    *                   the [[io.hemin.engine.model.SearchResult]].
    * @return The [[io.hemin.engine.model.SearchResult]] matching the query/page/size parameters.
    */
  final def search(query: String, pageNumber: Int, pageSize: Int): Future[SearchResult] = Future {
    if (isNullOrEmpty(query) || pageNumber < 1 || pageSize < 1) {
      SearchResult.empty
    } else {
      searchIndex(query, pageNumber, pageSize)
    }
  }

}
