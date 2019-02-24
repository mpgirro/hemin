package hemin.engine.searcher

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.Timeout
import hemin.engine.model.SearchResult
import hemin.engine.node.Node.{ActorRefSupervisor, ReportSearcherInitializationComplete}
import hemin.engine.searcher.Searcher.{SearchRequest, SearchResults}
import hemin.engine.{HeminConfig, HeminEngine}
import org.scalatest.{FlatSpecLike, Ignore, Matchers}

import scala.util.{Failure, Success}

@Ignore
class SearcherSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers {

  val engineConfig: HeminConfig = HeminConfig.defaultEngineConfig

  implicit val timeout: Timeout = engineConfig.node.internalTimeout

  val defaultQuery: String = "foo"
  val defaultPageNumber: Option[Int] = Some(1)
  val defaultPageSize: Option[Int] = Some(1)

  def assertEmptySearchResult(result: SearchResult): Unit = {
    result.currPage shouldBe 0
    result.maxPage shouldBe 0
    result.totalHits shouldBe 0
    result.results shouldBe Nil
  }

  def defaultTestSearcher(): TestActorRef[Searcher] = {
    val searcher: TestActorRef[Searcher] = TestActorRef(system.actorOf(Searcher.props(engineConfig.searcher)))
    searcher ! ActorRefSupervisor(testActor)
    expectMsgType[ReportSearcherInitializationComplete.type]
    searcher
  }

  "The Searcher" should "report its completed initialization" in {
    val searcher: ActorRef = system.actorOf(Searcher.props(engineConfig.searcher))
    searcher ! ActorRefSupervisor(testActor)
    expectMsgType[ReportSearcherInitializationComplete.type]
  }

  it should "reply with no search results found if the query is empty" in {
    val searcher: ActorRef = defaultTestSearcher()
    val future = searcher ? SearchRequest("", defaultPageNumber, defaultPageSize)
    future.value.get match {
      case Success(result: SearchResults) => assertEmptySearchResult(result.results)
      case Success(other) => fail("Expected reply was not of type : " + classOf[SearchResults])
      case Failure(ex) => throw ex
    }
  }

  it should "reply with no search results found if the query is null" in {
    val searcher: ActorRef = defaultTestSearcher()
    val future = searcher ? SearchRequest(null, defaultPageNumber, defaultPageSize)
    future.value.get match {
      case Success(result: SearchResults) => assertEmptySearchResult(result.results)
      case Success(other) => fail("Expected reply was not of type : " + classOf[SearchResults])
      case Failure(ex) => throw ex
    }
  }

  it should "reply with no search results found if the page number is invalid (<1)" in {
    val searcher: ActorRef = defaultTestSearcher()
    val future = searcher ? SearchRequest(defaultQuery, Some(0), defaultPageSize)
    future.value.get match {
      case Success(result: SearchResults) => assertEmptySearchResult(result.results)
      case Success(other) => fail("Expected reply was not of type : " + classOf[SearchResults])
      case Failure(ex) => throw ex
    }
  }

  it should "reply with no search results found if the page size is invalid (<1)" in {
    val searcher: ActorRef = defaultTestSearcher()
    val future = searcher ? Searcher.SearchRequest(defaultQuery, defaultPageNumber, Some(0))
    future.value.get match {
      case Success(result: SearchResults) => assertEmptySearchResult(result.results)
      case Success(other) => fail("Expected reply was not of type : " + classOf[SearchResults])
      case Failure(ex) => throw ex
    }
  }

}
