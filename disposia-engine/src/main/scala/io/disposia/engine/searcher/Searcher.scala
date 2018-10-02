package io.disposia.engine.searcher

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.google.common.base.Strings.isNullOrEmpty
import io.disposia.engine.EngineProtocol.{ActorRefSupervisor, ReportSearcherStartupComplete}
import io.disposia.engine.domain.{ImmutableResultWrapper, IndexDoc, IndexField, ResultWrapper}
import io.disposia.engine.index.IndexConfig
import io.disposia.engine.mapper.IndexMapper
import io.disposia.engine.searcher.Searcher.{SearcherRequest, SearcherResults}
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.{SolrClient, SolrQuery}
import org.apache.solr.common.SolrDocumentList

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Searcher {
  final val name = "searcher"
  def props(config: IndexConfig): Props =
    Props(new Searcher(config)).withDispatcher("echo.searcher.dispatcher")

  trait SearcherMessage
  trait SearcherQuery extends SearcherMessage
  trait SearcherQueryResult extends SearcherMessage
  // SearchQueries
  case class SearcherRequest(query: String, page: Int, size: Int) extends SearcherQuery
  // SearchQueryResults
  case class SearcherResults(results: ResultWrapper) extends SearcherQueryResult
}

class Searcher (config: IndexConfig)
  extends Actor with ActorLogging {

  log.debug("{} running on dispatcher {}", self.path.name, context.props.dispatcher)

  private implicit val executionContext: ExecutionContext = context.system.dispatchers.lookup("echo.searcher.dispatcher")

  private var supervisor: ActorRef = _

  private val solr: SolrClient = new HttpSolrClient.Builder(config.solrUri)
    .allowCompression(false) // TODO
    .build()

  private val indexMapper = IndexMapper.INSTANCE

  private val searchFields = List(
    IndexField.TITLE,
    IndexField.DESCRIPTION,
    IndexField.PODCAST_TITLE,
    IndexField.CONTENT_ENCODED,
    IndexField.TRANSCRIPT,
    IndexField.WEBSITE_DATA,
    IndexField.ITUNES_AUTHOR,
    IndexField.ITUNES_SUMMARY,
    IndexField.CHAPTER_MARKS)

  override def postStop: Unit = {

    log.info("shutting down")
  }

  override def receive: Receive = {

    case ActorRefSupervisor(ref) =>
      log.debug("Received ActorRefSupervisor(_)")
      supervisor = ref
      supervisor ! ReportSearcherStartupComplete

    case SearcherRequest(query, page, size) =>
      log.debug("Received SearchRequest('{}',{},{}) message", query, page, size)

      val theSender = sender()
      searchSolr(query, page, size, queryOperator=Some("AND"), None, None)
        .onComplete {
          case Success(rs) => theSender ! SearcherResults(rs)
          case Failure(ex) =>
            log.error("Error on querying Solr : {}", ex)
            ex.printStackTrace()
        }

    case unhandled => log.warning("Received unhandled message of type : {}", unhandled.getClass)
  }

  private def buildQuery(query: String): String = {
    val q = query.trim
    searchFields
      .map(f => s"$f:($q)")
      .mkString(" ")
  }

  private def searchSolr(q: String, p: Int, s: Int, queryOperator: Option[String], minMatch: Option[String], sort: Option[String]): Future[ResultWrapper] = Future {

    if (isNullOrEmpty(q)) return Future { ResultWrapper.empty() }
    if (p < 1)            return Future { ResultWrapper.empty() }
    if (s < 1)            return Future { ResultWrapper.empty() }

    val offset = (p-1) * s

    val query = new SolrQuery()
      .setStart(offset)
      .setParam("defType", "edismax")
      .setQuery(buildQuery(q))
      .setRows(s)

    // When you assign mm (Minimum 'Should' Match), we remove q.op
    // because we can't set two params to the same function
    // q.op=AND == mm=100% | q.op=OR == mm=0%
    /*
    minMatch match {
      case Some(mm) => query.setParam("mm", mm) // "100%"
      case None =>
        query.setParam("q.op", queryOperator.getOrElse("AND"))
    }
    */

    query.setParam("qf", "title^1 podcast_title^1 chapter_marks^1 description^1e-13 itunes_summary^1e-13 itunes_author^1e-13 content_encoded^1e-13 transcript^1e-13 website_data^1e-13")

    /*
    query.setQuery(q.trim) // Strip whitespace (or other characters) from the beginning and end of a string
    query.setFields(
      IndexField.TITLE,
      IndexField.DESCRIPTION,
      IndexField.LINK,
      IndexField.PODCAST_TITLE,
      IndexField.CONTENT_ENCODED,
      IndexField.TRANSCRIPT,
      IndexField.WEBSITE_DATA,
      IndexField.ITUNES_AUTHOR,
      IndexField.ITUNES_SUMMARY,
      IndexField.CHAPTER_MARKS)
      */
    //query.setQuery(buildQuery(q))


    val response: QueryResponse = solr.query(query)
    val docList: SolrDocumentList = response.getResults()


    val resultWrapper = ImmutableResultWrapper.builder

    // set some sane values, we'll overwrite these if all goes well
    resultWrapper.setCurrPage(0)
    resultWrapper.setMaxPage(0)
    resultWrapper.setTotalHits(0)

    if (docList.getNumFound <= 0) {
      resultWrapper.setResults(List().asJava)
    } else {
      val resultDocs: Array[IndexDoc] = new Array[IndexDoc](docList.getNumFound.toInt)
      for ((d,i) <- docList.asScala.zipWithIndex) {
        resultDocs(i) = indexMapper.toImmutable(d)
      }

      resultWrapper.setCurrPage(p)
      resultWrapper.setTotalHits(docList.getNumFound.toInt)
      resultWrapper.setResults(resultDocs.toList.asJava)
    }

    resultWrapper.create

    /*
    val qOp = queryOperator.getOrElse("AND")
    val mm = minMatch.getOrElse("<NULL>").replace("\"", "")
    val solrSort = sort.getOrElse("<NULL>")

    // Strip whitespace (or other characters) from the beginning and end of a string
    val query = q.trim

    val offset = (p-1) * s

    var queryBuilder = solr
      .query(query)
      .setParameter("bf", "product(recip(sub(" + timestamp + ",record_posted_time),1.27e-11,0.08,0.05),1000)^50")
      .setParameter("defType", "edismax")
      .start(offset)
      .rows(s)

    // When you assign mm (Minimum 'Should' Match), we remove q.op
    // because we can't set two params to the same function
    // q.op=AND == mm=100% | q.op=OR == mm=0%
    if (!mm.equals("<NULL>")) {
      queryBuilder = queryBuilder.setParameter("mm", "100%")
    } else {
      queryBuilder = queryBuilder.setParameter("q.op", queryOperator)
    }

    if (!query.equals("*:*")) {
      queryBuilder = queryBuilder.setParameter("qf", "title^1 description^1e-13 location^1e-13 tag_1_name^1e-13 tag_2_name^1  city_name^1 price^1")
      queryBuilder = queryBuilder.setParameter("bf", "product(recip(rord(record_posted_day),1,1000,1000),400)^60")
    }
    if (!date_from.equals("<NULL>") && !date_to.equals("<NULL>")) {
      queryBuilder = queryBuilder.facetFields("record_posted_time").addFilterQuery("record_posted_time:[" + date_from + " TO " + date_to + "]")
    }

    queryBuilder
    */

  }

}
