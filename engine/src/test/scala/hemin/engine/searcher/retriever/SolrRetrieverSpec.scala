package hemin.engine.searcher.retriever

import java.util.concurrent.Executors

import hemin.engine.HeminConfig
import hemin.engine.model.SearchResult
import org.scalatest.{FlatSpec, Ignore, Matchers}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Ignore
class SolrRetrieverSpec
  extends FlatSpec
    with Matchers {

  implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

  val engineConfig: HeminConfig = HeminConfig.defaultEngineConfig

  val defaultQuery: String = "foo"
  val defaultPageNumber: Int = 1
  val defaultPageSize: Int = 1

  def assertEmptySearchResult(result: SearchResult): Unit = {
    result.currPage shouldBe 0
    result.maxPage shouldBe 0
    result.totalHits shouldBe 0
    result.results shouldBe Nil
  }

  "The SolrRetriever" should "succeed to initialize when connecting to the Solr mock server" in {
    val retriever: IndexRetriever = new SolrRetriever(engineConfig.searcher, executionContext)
    // TODO add an assertion?
  }

  it should "retrieve empty search results if the query is empty" in {
    val retriever: IndexRetriever = new SolrRetriever(engineConfig.searcher, executionContext)
    val future: Future[SearchResult] = retriever.search(
      query = "",
      page = defaultPageNumber,
      size = defaultPageSize
    )
    future.value.get match {
      case Success(result) => assertEmptySearchResult(result)
      case Failure(ex) => throw ex
    }
  }


  it should "retrieve empty search results if the query is null" in {
    val retriever: IndexRetriever = new SolrRetriever(engineConfig.searcher, executionContext)
    val future: Future[SearchResult] = retriever.search(
      query = null,
      page = defaultPageNumber,
      size = defaultPageSize
    )
    future.value.get match {
      case Success(result) => assertEmptySearchResult(result)
      case Failure(ex) => throw ex
    }
  }

  it should "retrieve empty search results if the page number is invalid (<1)" in {
    val retriever: IndexRetriever = new SolrRetriever(engineConfig.searcher, executionContext)
    val future: Future[SearchResult] = retriever.search(
      query = defaultQuery,
      page = 0,
      size = defaultPageSize
    )
    future.value.get match {
      case Success(result) => assertEmptySearchResult(result)
      case Failure(ex) => throw ex
    }
  }


  it should "retrieve empty search results if the page size is invalid (<1)" in {
    val retriever: IndexRetriever = new SolrRetriever(engineConfig.searcher, executionContext)
    val future: Future[SearchResult] = retriever.search(
      query = defaultQuery,
      page = defaultPageNumber,
      size = 0
    )
    future.value.get match {
      case Success(result) => assertEmptySearchResult(result)
      case Failure(ex) => throw ex
    }
  }

}
