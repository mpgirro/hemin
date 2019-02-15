package hemin.engine.searcher.retriever

import com.google.common.base.Strings.isNullOrEmpty
import hemin.engine.model.SearchResult
import hemin.engine.searcher.SearcherConfig

import scala.concurrent.{ExecutionContext, Future}

trait IndexRetriever {

  protected[this] implicit def executionContext: ExecutionContext

  protected[this] def searcherConfig: SearcherConfig

  protected[this] def searchIndex(q: String, p: Int, s: Int): SearchResult

  /** Searches the reverse index for the given query. Returns an eventual
    * [[hemin.engine.model.SearchResult]] for the page and size parameters.
    *
    * @param query The query to search the reverse index for.
    * @param page  The page for the [[hemin.engine.model.SearchResult]]. If None, then
    *              [[hemin.engine.searcher.SearcherConfig.defaultPage]] is used.
    * @param size  The size (= maximum number of elements in the
    *              [[hemin.engine.model.SearchResult.results]] list) of the
    *              [[hemin.engine.model.SearchResult]]. If None, then
    *              [[hemin.engine.searcher.SearcherConfig.defaultSize]] is used.
    * @return The [[hemin.engine.model.SearchResult]] matching the query/page/size parameters.
    */
  final def search(query: String, page: Option[Int], size: Option[Int]): Future[SearchResult] = {
    val p: Int = page.getOrElse(searcherConfig.defaultPage)
    val s: Int = size.getOrElse(searcherConfig.defaultSize)
    search(query, p, s)
  }

  /** Searches the reverse index for the given query. Returns an eventual
    * [[hemin.engine.model.SearchResult]] for the page and size parameters.
    *
    * @param query The query to search the reverse index for.
    * @param page  The page for the [[hemin.engine.model.SearchResult]].
    * @param size  The size (= maximum number of elements in the
    *              [[hemin.engine.model.SearchResult.results]] list) of
    *              the [[hemin.engine.model.SearchResult]].
    * @return The [[hemin.engine.model.SearchResult]] matching the query/page/size parameters.
    */
  final def search(query: String, page: Int, size: Int): Future[SearchResult] = Future {
    if (isNullOrEmpty(query) || page < 1 || size < 1) {
      SearchResult.empty
    } else {
      searchIndex(query, page, size)
    }
  }

}
