package io.hemin.engine.searcher.retriever

import com.typesafe.scalalogging.Logger
import io.hemin.engine.model.{IndexDoc, IndexField, ResultPage}
import io.hemin.engine.searcher.SearcherConfig
import io.hemin.engine.model.IndexField._
import io.hemin.engine.util.mapper.IndexMapper
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.{SolrClient, SolrQuery}
import org.apache.solr.common.SolrDocumentList

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}


class SolrRetriever (config: SearcherConfig,
                     ec: ExecutionContext)
  extends IndexRetriever {

  private val log = Logger(getClass)

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] def searcherConfig: SearcherConfig = config

  override protected[this] def searchIndex(q: String, p: Int, s: Int): ResultPage =
    searchSolr(q, p, s, queryOperator = Some("AND"), minMatch = None, sort = None)

  private val solr: SolrClient = new HttpSolrClient.Builder(config.solrUri)
    .allowCompression(false) // TODO config and why don't I want =true?
    .build()

  private def assess(field: IndexField): Double = field match {
    case DocType        => 0
    case Id             => 0
    case Title          => 1
    case Description    => 1e-13
    case Link           => 0
    case PubDate        => 0
    case PodcastTitle   => 1
    case ItunesImage    => 0
    case ItunesDuration => 0
    case ItunesAuthor   => 1e-13
    case ItunesSummary  => 1e-13
    case ChapterMarks   => 1
    case ContentEncoded => 1e-13
    case Transcript     => 1e-13
    case WebsiteData    => 1e-13
  }

  // edismax takes a "qf" parameter of the form "title^1 description^1e-13 ..."
  private lazy val queryFieldsAssessments: String = IndexField.values
    .map(x => s"${x.entryName}^${assess(x)}")
    .mkString(" ")

  private def buildQuery(query: String): String = IndexField.values
    .map(f => s"${f.entryName}:(${query.trim})")
    .mkString(" ")

  private def toResults(docs: SolrDocumentList): List[IndexDoc] = {
    val (successes, failures) = docs
      .asScala
      .map(IndexMapper.toIndexDoc)
      .partition(_.isSuccess)

    // report all failures
    failures
      .map(_.failed.get) // ensure a Seq[Failure[IndexDoc]]
      .foreach { ex =>
        log.error("Failed to map Solr result to IndexDoc; reason : {}", ex.getMessage)
        ex.printStackTrace()
      }

    successes
      .map(_.get)
      .toList
  }

  private def searchSolr(q: String, p: Int, s: Int, queryOperator: Option[String], minMatch: Option[String], sort: Option[String]): ResultPage = {

    // TODO sort is unused (and never set in caller)

    val offset = (p-1) * s

    val query = new SolrQuery()
      .setStart(offset)
      .setParam("defType", "edismax")
      .setQuery(buildQuery(q))
      .setRows(s)
      .setParam("qf", queryFieldsAssessments)

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

    val rs: SolrDocumentList = solr.query(query).getResults

    if (rs.getNumFound <= 0) {
      ResultPage.empty // default parameters relate to nothing found
    } else {
      val dMaxPage = rs.getNumFound.toDouble / s.toDouble
      val mp = Math.ceil(dMaxPage).toInt
      val maxPage = if (mp == 0 && p == 1) 1 else mp

      ResultPage(
        currPage  = p,
        maxPage   = maxPage, // TODO
        totalHits = rs.getNumFound.toInt,
        results   = toResults(rs),
      )
    }

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
