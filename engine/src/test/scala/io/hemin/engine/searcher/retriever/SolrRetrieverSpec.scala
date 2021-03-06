package io.hemin.engine.searcher.retriever

import io.hemin.engine.TestConstants
import io.hemin.engine.model.SearchResult
import io.hemin.engine.searcher.SearcherConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class SolrRetrieverSpec
  extends FlatSpec
    with ScalaFutures
    with Matchers {

  implicit val executionContext: ExecutionContext = TestConstants.executionContext

  val searcherConfig: SearcherConfig = TestConstants.engineConfig.searcher

  val defaultQuery: String = "foo"
  val defaultPageNumber: Int = 1
  val defaultPageSize: Int = 1

  def assertEmptySearchResult(result: SearchResult): Unit = {
    result.currPage shouldBe 0
    result.maxPage shouldBe 0
    result.totalHits shouldBe 0
    result.results shouldBe Nil
  }

  "The SolrRetriever" should "succeed to initialize when connecting to the Solr server in a Unit Test" in {
    val retriever: IndexRetriever = new SolrRetriever(searcherConfig, executionContext)
    // TODO add an assertion?
  }

  it should "retrieve empty search results if the query is empty" in {
    val retriever: IndexRetriever = new SolrRetriever(searcherConfig, executionContext)
    val future: Future[SearchResult] = retriever.search(
      query      = "",
      pageNumber = defaultPageNumber,
      pageSize   = defaultPageSize
    )
    future.map(assertEmptySearchResult)
  }


  it should "retrieve empty search results if the query is null" in {
    val retriever: IndexRetriever = new SolrRetriever(searcherConfig, executionContext)
    val future: Future[SearchResult] = retriever.search(
      query      = null,
      pageNumber = defaultPageNumber,
      pageSize   = defaultPageSize
    )
    future.map(assertEmptySearchResult)
  }

  it should "retrieve empty search results if the page number is invalid (<1)" in {
    val retriever: IndexRetriever = new SolrRetriever(searcherConfig, executionContext)
    val future: Future[SearchResult] = retriever.search(
      query      = defaultQuery,
      pageNumber = 0,
      pageSize   = defaultPageSize
    )
    future.map(assertEmptySearchResult)
  }


  it should "retrieve empty search results if the page size is invalid (<1)" in {
    val retriever: IndexRetriever = new SolrRetriever(searcherConfig, executionContext)
    val future: Future[SearchResult] = retriever.search(
      query      = defaultQuery,
      pageNumber = defaultPageNumber,
      pageSize   = 0
    )
    future.map(assertEmptySearchResult)
  }

}
