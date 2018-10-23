package io.hemin.engine.searcher.retriever

import io.hemin.engine.domain.{IndexField, ResultsWrapper}
import io.hemin.engine.searcher.SearcherConfig
import io.hemin.engine.util.mapper.IndexMapper
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.{SolrClient, SolrQuery}
import org.apache.solr.common.SolrDocumentList

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext


class SolrRetriever (config: SearcherConfig, ec: ExecutionContext) extends IndexRetriever {

  override protected[this] implicit def executionContext: ExecutionContext = ec

  override protected[this] def searcherConfig: SearcherConfig = config

  override protected[this] def searchIndex(q: String, p: Int, s: Int): ResultsWrapper =
    searchSolr(q, p, s, queryOperator=Some("AND"), minMatch=None, sort=None)

  private val solr: SolrClient = new HttpSolrClient.Builder(config.solrUri)
    .allowCompression(false) // TODO config and why don't I want =true?
    .build()

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

  private def buildQuery(query: String): String = {
    val q = query.trim
    searchFields
      .map(f => s"$f:($q)")
      .mkString(" ")
  }

  private def searchSolr(q: String, p: Int, s: Int, queryOperator: Option[String], minMatch: Option[String], sort: Option[String]): ResultsWrapper = {

    // TODO sort is unused (and never set in caller)

    val offset = (p-1) * s

    val query = new SolrQuery()
      .setStart(offset)
      .setParam("defType", "edismax")
      .setQuery(buildQuery(q))
      .setRows(s)
      .setParam("qf", "title^1 podcast_title^1 chapter_marks^1 description^1e-13 itunes_summary^1e-13 itunes_author^1e-13 content_encoded^1e-13 transcript^1e-13 website_data^1e-13")

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

    val results: SolrDocumentList = solr.query(query).getResults

    if (results.getNumFound <= 0) {
      ResultsWrapper() // default parameters relate to nothing found
    } else {
      val dMaxPage = results.getNumFound.toDouble / s.toDouble
      val mp = Math.ceil(dMaxPage).toInt
      val maxPage = if (mp == 0 && p == 1) 1 else mp

      ResultsWrapper(
        currPage  = p,
        maxPage   = maxPage, // TODO
        totalHits = results.getNumFound.toInt,
        results   = results.asScala.map(IndexMapper.toIndexDoc).toList,
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
