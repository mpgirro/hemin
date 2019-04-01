package hemin.engine.searcher

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import hemin.engine.model.SearchResult
import hemin.engine.node.Node.{ActorRefSupervisor, ReportSearcherInitializationComplete}
import hemin.engine.searcher.Searcher.{SearchRequest, SearchReply}
import hemin.engine.{HeminEngine, TestConstants}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.ExecutionContext

class SearcherSpec
  extends TestKit(ActorSystem(HeminEngine.name))
    with ImplicitSender
    with FlatSpecLike
    with Matchers
    with ScalaFutures {

  val searcherConfig: SearcherConfig = TestConstants.engineConfig.searcher

  implicit val executionContext: ExecutionContext = TestConstants.executionContext
  implicit val timeout: Timeout = TestConstants.timeout

  val defaultQuery: String = "foo"
  val defaultPageNumber: Option[Int] = Some(1)
  val defaultPageSize: Option[Int] = Some(1)

  def assertEmptySearchResult(result: SearchResult): Unit = {
    result.currPage shouldBe 0
    result.maxPage shouldBe 0
    result.totalHits shouldBe 0
    result.results shouldBe Nil
  }

  def defaultTestSearcher(): ActorRef = {
    val searcher: ActorRef = system.actorOf(Searcher.props(searcherConfig))
    searcher ! ActorRefSupervisor(testActor)
    expectMsgType[ReportSearcherInitializationComplete.type]
    searcher
  }

  "The Searcher" should "report its completed initialization" in {
    val searcher: ActorRef = system.actorOf(Searcher.props(searcherConfig))
    searcher ! ActorRefSupervisor(testActor)
    expectMsgType[ReportSearcherInitializationComplete.type]
  }

  it should "reply with empty search results if the query is empty" in {
    val searcher: ActorRef = defaultTestSearcher()
    (searcher ? SearchRequest("", defaultPageNumber, defaultPageSize))
      .mapTo[SearchReply]
      .map(_.results)
      .map(assertEmptySearchResult)
  }

  it should "reply with empty search results if the query is null" in {
    val searcher: ActorRef = defaultTestSearcher()
    (searcher ? SearchRequest(null, defaultPageNumber, defaultPageSize))
      .mapTo[SearchReply]
      .map(_.results)
      .map(assertEmptySearchResult)
  }

  it should "reply with empty search results if the page number is invalid (<1)" in {
    val searcher: ActorRef = defaultTestSearcher()
    (searcher ? SearchRequest(defaultQuery, Some(0), defaultPageSize))
      .mapTo[SearchReply]
      .map(_.results)
      .map(assertEmptySearchResult)
  }

  it should "reply with empty search results if the page size is invalid (<1)" in {
    val searcher: ActorRef = defaultTestSearcher()
    (searcher ? SearchRequest(defaultQuery, defaultPageNumber, Some(0)))
      .mapTo[SearchReply]
      .map(_.results)
      .map(assertEmptySearchResult)
  }

}
